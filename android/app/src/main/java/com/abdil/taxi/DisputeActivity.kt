package com.abdil.taxi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.abdil.taxi.model.DisputeRequest
import com.abdil.taxi.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DisputeActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var spinnerReasons: Spinner
    private lateinit var etDescription: EditText
    private lateinit var etRideId: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnHistory: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var cardHistory: View

    private val reasons = listOf(
        "💰 Problème de paiement (montant incorrect)",
        "🚗 Chauffeur non professionnel",
        "⏰ Retard important",
        "🗺️ Mauvais itinéraire",
        "🔒 Problème de sécurité",
        "📱 Problème technique (application)",
        "👤 Problème avec un autre passager",
        "📞 Problème de communication",
        "🧾 Autre problème"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dispute)

        tokenManager = TokenManager(this)

        initViews()
        setupSpinner()
        setupClickListeners()
        loadDisputeHistory()
    }

    private fun initViews() {
        spinnerReasons = findViewById(R.id.spinnerReasons)
        etDescription = findViewById(R.id.etDescription)
        etRideId = findViewById(R.id.etRideId)
        btnSubmit = findViewById(R.id.btnSubmit)
        btnHistory = findViewById(R.id.btnHistory)
        progressBar = findViewById(R.id.progressBar)
        tvStatus = findViewById(R.id.tvStatus)
        cardHistory = findViewById(R.id.cardHistory)
    }

    private fun setupSpinner() {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, reasons)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerReasons.adapter = adapter
    }

    private fun setupClickListeners() {
        btnSubmit.setOnClickListener {
            submitDispute()
        }

        btnHistory.setOnClickListener {
            loadDisputeHistory()
        }
    }

    private fun submitDispute() {
        val rideIdStr = etRideId.text.toString().trim()
        val reason = spinnerReasons.selectedItem.toString()
        val description = etDescription.text.toString().trim()

        if (rideIdStr.isEmpty()) {
            Toast.makeText(this, "Veuillez entrer le numéro de course", Toast.LENGTH_SHORT).show()
            return
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Veuillez décrire votre problème", Toast.LENGTH_SHORT).show()
            return
        }

        val rideId = rideIdStr.toLongOrNull()
        if (rideId == null || rideId <= 0) {
            Toast.makeText(this, "Numéro de course invalide", Toast.LENGTH_SHORT).show()
            return
        }

        val clientId = tokenManager.getUserId()
        if (clientId == -1L) {
            Toast.makeText(this, "Erreur d'identification", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnSubmit.isEnabled = false

        val request = DisputeRequest(
            rideId = rideId,
            clientId = clientId,
            reason = reason,
            description = description
        )

        RetrofitClient.apiService.submitDispute(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true

                if (response.isSuccessful && response.body() != null) {
                    val result = response.body()!!
                    val success = result["success"] as? Boolean ?: false
                    val message = result["message"] as? String ?: ""

                    if (success) {
                        AlertDialog.Builder(this@DisputeActivity)
                            .setTitle("✅ Litige envoyé")
                            .setMessage("Votre litige a été enregistré avec succès. Nous reviendrons vers vous dans les plus brefs délais.\n\n📋 Référence: #${result["disputeId"]}")
                            .setPositiveButton("OK") { _, _ ->
                                etRideId.text.clear()
                                etDescription.text.clear()
                                loadDisputeHistory()
                            }
                            .show()
                    } else {
                        Toast.makeText(this@DisputeActivity, "❌ $message", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this@DisputeActivity, "Erreur de communication", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                progressBar.visibility = View.GONE
                btnSubmit.isEnabled = true
                Toast.makeText(this@DisputeActivity, "Erreur: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadDisputeHistory() {
        val clientId = tokenManager.getUserId()
        if (clientId == -1L) return

        progressBar.visibility = View.VISIBLE

        RetrofitClient.apiService.getClientDisputes(clientId).enqueue(object : Callback<List<Map<String, Any>>> {
            override fun onResponse(call: Call<List<Map<String, Any>>>, response: Response<List<Map<String, Any>>>) {
                progressBar.visibility = View.GONE

                if (response.isSuccessful && response.body() != null) {
                    val disputes = response.body()!!
                    if (disputes.isEmpty()) {
                        tvStatus.text = "📭 Aucun litige enregistré"
                        tvStatus.visibility = View.VISIBLE
                        cardHistory.visibility = View.GONE
                        return
                    }

                    cardHistory.visibility = View.VISIBLE
                    tvStatus.visibility = View.GONE

                    val historyText = StringBuilder("📋 Historique des litiges\n${"=".repeat(30)}\n\n")
                    disputes.forEachIndexed { index, dispute ->
                        val status = when (dispute["status"]) {
                            "RESOLVED" -> "✅ Résolu"
                            "REJECTED" -> "❌ Rejeté"
                            else -> "⏳ En attente"
                        }
                        historyText.append("${index + 1}. #${dispute["id"]} - ${dispute["reason"]}\n")
                        historyText.append("   Statut: $status\n")
                        historyText.append("   ${dispute["createdAt"]}\n\n")
                    }

                    AlertDialog.Builder(this@DisputeActivity)
                        .setTitle("📋 Historique des litiges")
                        .setMessage(historyText.toString())
                        .setPositiveButton("Fermer", null)
                        .show()
                } else {
                    Toast.makeText(this@DisputeActivity, "Erreur chargement historique", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Map<String, Any>>>, t: Throwable) {
                progressBar.visibility = View.GONE
                Toast.makeText(this@DisputeActivity, "Erreur: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}