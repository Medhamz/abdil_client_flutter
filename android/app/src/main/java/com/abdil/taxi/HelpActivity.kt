package com.abdil.taxi

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HelpActivity : AppCompatActivity() {

    private lateinit var tokenManager: TokenManager
    private lateinit var searchView: SearchView
    private lateinit var rvHelpItems: RecyclerView
    private lateinit var btnFaq: Button
    private lateinit var btnSupport: Button
    private lateinit var btnTutorial: Button
    private lateinit var tvWelcome: TextView

    private lateinit var helpAdapter: HelpAdapter
    private val allHelpItems = mutableListOf<HelpItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_help)

        tokenManager = TokenManager(this)

        initViews()
        setupData()
        setupListeners()
        displayWelcomeMessage()
    }

    private fun initViews() {
        searchView = findViewById(R.id.searchView)
        rvHelpItems = findViewById(R.id.rvHelpItems)
        btnFaq = findViewById(R.id.btnFaq)
        btnSupport = findViewById(R.id.btnSupport)
        btnTutorial = findViewById(R.id.btnTutorial)
        tvWelcome = findViewById(R.id.tvWelcome)

        rvHelpItems.layoutManager = LinearLayoutManager(this)
        helpAdapter = HelpAdapter { item ->
            showHelpDetail(item)
        }
        rvHelpItems.adapter = helpAdapter
    }

    private fun setupData() {
        allHelpItems.addAll(listOf(
            HelpItem(
                icon = "🚖",
                title = "Comment réserver une course ?",
                description = "1. Entrez votre destination\n2. Choisissez le type de course\n3. Sélectionnez le mode de paiement\n4. Cliquez sur Réserver",
                category = "Réservation"
            ),
            HelpItem(
                icon = "💳",
                title = "Modes de paiement disponibles",
                description = "• Espèces\n• Porte-monnaie électronique\n• QR Code\n• Paiement par lien\n• NFC",
                category = "Paiement"
            ),
            HelpItem(
                icon = "👤",
                title = "Créer et gérer son compte",
                description = "L'inscription se fait via l'écran d'accueil. Vous pouvez modifier vos informations dans les paramètres.",
                category = "Compte"
            ),
            HelpItem(
                icon = "🗺️",
                title = "Suivi de course en temps réel",
                description = "Pendant votre course, vous pouvez suivre la position du chauffeur sur la carte. Partagez le lien de suivi avec vos proches.",
                category = "Course"
            ),
            HelpItem(
                icon = "💬",
                title = "Chat avec le chauffeur",
                description = "Une fois votre course acceptée, un bouton Chat apparaît. Vous pouvez envoyer des messages, des images et des audios.",
                category = "Communication"
            ),
            HelpItem(
                icon = "💰",
                title = "Porte-monnaie électronique",
                description = "Rechargez votre porte-monnaie pour payer vos courses plus rapidement. Utilisez des coupons pour recharger.",
                category = "Paiement"
            ),
            HelpItem(
                icon = "📢",
                title = "Taxi Pub - Publier une annonce",
                description = "Vous pouvez publier une annonce publicitaire sur les taxis. Remplissez le formulaire et validez.",
                category = "Publicité"
            ),
            HelpItem(
                icon = "⚖️",
                title = "Gestion des litiges",
                description = "En cas de problème, utilisez la section Litiges pour soumettre une réclamation. Nous traitons votre demande rapidement.",
                category = "Support"
            ),
            HelpItem(
                icon = "🆘",
                title = "SOS - Urgence",
                description = "Le bouton SOS vous permet d'alerter les services d'urgence en cas de problème pendant votre course.",
                category = "Sécurité"
            ),
            HelpItem(
                icon = "📅",
                title = "Programmer une course",
                description = "Vous pouvez programmer une course à l'avance. Choisissez la date et l'heure de votre course.",
                category = "Réservation"
            )
        ))

        helpAdapter.submitList(allHelpItems)
    }

    private fun setupListeners() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                filterItems(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filterItems(newText)
                return true
            }
        })

        btnFaq.setOnClickListener {
            filterItems("")
            searchView.setQuery("", false)
            Toast.makeText(this, "📋 FAQ - Questions fréquentes", Toast.LENGTH_SHORT).show()
        }

        btnSupport.setOnClickListener {
            showSupportDialog()
        }

        btnTutorial.setOnClickListener {
            showTutorialDialog()
        }
    }

    private fun displayWelcomeMessage() {
        val name = tokenManager.getUserName() ?: "Cher utilisateur"
        tvWelcome.text = "👋 Bonjour $name, comment pouvons-nous vous aider ?"
    }

    private fun filterItems(query: String) {
        if (query.isEmpty()) {
            helpAdapter.submitList(allHelpItems)
            return
        }

        val filtered = allHelpItems.filter {
            it.title.lowercase().contains(query.lowercase()) ||
                    it.description.lowercase().contains(query.lowercase()) ||
                    it.category.lowercase().contains(query.lowercase())
        }
        helpAdapter.submitList(filtered)
    }

    private fun showHelpDetail(item: HelpItem) {
        AlertDialog.Builder(this)
            .setTitle("${item.icon} ${item.title}")
            .setMessage(Html.fromHtml(item.description.replace("\n", "<br>"), Html.FROM_HTML_MODE_LEGACY))
            .setPositiveButton("Compris", null)
            .setNeutralButton("💬 Contacter le support") { _, _ ->
                showSupportDialog()
            }
            .show()
    }

    private fun showSupportDialog() {
        AlertDialog.Builder(this)
            .setTitle("📞 Contacter le support")
            .setMessage("""
                |📧 Email: support@abdiltaxi.com
                |📱 Téléphone: +227 94 00 89 58
                |💬 WhatsApp: +227 94 00 89 58
                |🕐 Horaires: 24h/24, 7j/7
                |
                |✉️ Répondez à ce message pour envoyer un email de support.
            """.trimMargin())
            .setPositiveButton("Appeler") { _, _ ->
                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:+22794008958")
                }
                startActivity(intent)
            }
            .setNeutralButton("Email") { _, _ ->
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:support@abdiltaxi.com")
                    putExtra(Intent.EXTRA_SUBJECT, "[Abdil Taxi] Support client")
                }
                startActivity(intent)
            }
            .setNegativeButton("Fermer", null)
            .show()
    }

    private fun showTutorialDialog() {
        AlertDialog.Builder(this)
            .setTitle("🎓 Tutoriel interactif")
            .setMessage("""
                |🔹 Réservation: Choisissez départ/destination → Estimer → Réserver
                |🔹 Suivi: Carte en temps réel du chauffeur
                |🔹 Paiement: 5 modes disponibles (Espèces, Wallet, QR, Lien, NFC)
                |🔹 Litiges: Soumettez une réclamation en cas de problème
                |🔹 Taxi Pub: Publiez votre annonce publicitaire
                |🔹 SOS: Bouton d'urgence disponible
                |
                |📱 Notre équipe est à votre disposition 24h/24 !
            """.trimMargin())
            .setPositiveButton("Commencer", null)
            .show()
    }
}