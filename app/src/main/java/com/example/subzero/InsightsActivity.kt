package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subzero.network.ApiClient
import com.example.subzero.network.SubscriptionResponse
import com.example.subzero.views.DonutChartView
import com.example.subzero.views.DonutSlice
import kotlinx.coroutines.launch


open class InsightsActivity : AppCompatActivity() {

    // color palette for the page
    private val sliceColors = listOf(
        Color.parseColor("#E05252"),
        Color.parseColor("#4A90D9"),
        Color.parseColor("#F5A623"),
        Color.parseColor("#7ED321"),
        Color.parseColor("#9B59B6"),
        Color.parseColor("#1ABC9C"),
        Color.parseColor("#E67E22"),
        Color.parseColor("#E91E8C"),
    )
    // creates xml layout variables for the things to be rendered
    private lateinit var tvMonthlyTotal: TextView
    private lateinit var tvChangePercent: TextView
    private lateinit var tvChangeLabel: TextView
    private lateinit var donutChart: DonutChartView
    private lateinit var tvEmptyChart: TextView
    private lateinit var legendContainer: LinearLayout
    private lateinit var breakdownContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_insights) // telling app to draw activity_insights.xml
        initViews() // initiates views
        setupBottomNav()
        loadInsights()
    }

    // finds the views for each part of the UI
    private fun initViews() {
        tvMonthlyTotal     = findViewById(R.id.tvMonthlyTotal)
        tvChangePercent    = findViewById(R.id.tvChangePercent)
        tvChangeLabel      = findViewById(R.id.tvChangeLabel)
        donutChart         = findViewById(R.id.donutChart)
        tvEmptyChart       = findViewById(R.id.tvEmptyChart)
        legendContainer    = findViewById(R.id.legendContainer)
        breakdownContainer = findViewById(R.id.breakdownContainer)
    }

    // sets up click functions for navbar
    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navManage).setOnClickListener { /* nothing yet */ }
        findViewById<LinearLayout>(R.id.navInsights).setOnClickListener { /* already here */ }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            // no alerts page yet
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            navigateToProfile()
        }
    }

    // uses token for the user acc to load
    private fun loadInsights() {
        val token = SessionManager.getToken(this) ?: return

        lifecycleScope.launch {
            try {
                // grab the subscriptions for the given account
                val response = ApiClient.instance.getSubscriptions("Bearer $token")

                // error handling
                if (!response.isSuccessful) {
                    Toast.makeText(this@InsightsActivity, "Failed to load data", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // create a list w the subscriptions
                val subscriptions: List<SubscriptionResponse> = response.body() ?: emptyList()

                // rendering logic
                if (subscriptions.isEmpty()) {
                    showEmptyState()
                } else {
                    renderInsights(subscriptions)
                }

            } catch (e: Exception) {
                Toast.makeText(this@InsightsActivity, "Network error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun renderInsights(subscriptions: List<SubscriptionResponse>) {
        // monthly total
        val monthlyTotal: Double = subscriptions.sumOf { sub: SubscriptionResponse ->
            normaliseToMonthly(sub.cost, sub.billing_cycle)
        }
        tvMonthlyTotal.text  = "$%.2f".format(monthlyTotal)
        tvChangePercent.text = "↘ 0%"
        tvChangeLabel.text   = "Same as last month"

        // group by category
        val byCategory: Map<String, List<SubscriptionResponse>> =
            subscriptions.groupBy { sub: SubscriptionResponse -> sub.category }

        val categoryTotals: List<Pair<String, Double>> = byCategory
            .map { entry: Map.Entry<String, List<SubscriptionResponse>> ->
                val total: Double = entry.value.sumOf { sub: SubscriptionResponse ->
                    normaliseToMonthly(sub.cost, sub.billing_cycle)
                }
                Pair(entry.key, total)
            }
            .sortedByDescending { pair: Pair<String, Double> -> pair.second }

        // donut slices
        val slices: List<DonutSlice> = categoryTotals.mapIndexed { i: Int, pair: Pair<String, Double> ->
            DonutSlice(pair.second.toFloat(), sliceColors[i % sliceColors.size])
        }
        donutChart.slices      = slices
        donutChart.visibility  = View.VISIBLE
        tvEmptyChart.visibility = View.GONE

        // legend
        legendContainer.removeAllViews()
        categoryTotals.forEachIndexed { i: Int, pair: Pair<String, Double> ->
            legendContainer.addView(
                buildLegendRow(sliceColors[i % sliceColors.size], pair.first, pair.second)
            )
        }

        // breakdown
        breakdownContainer.removeAllViews()
        val sorted: List<SubscriptionResponse> =
            subscriptions.sortedByDescending { sub: SubscriptionResponse -> sub.cost }
        sorted.forEach { sub: SubscriptionResponse ->
            breakdownContainer.addView(buildBreakdownRow(sub))
        }
    }

    // empty placeholders
    private fun showEmptyState() {
        tvMonthlyTotal.text     = "$0.00"
        tvChangePercent.text    = ""
        tvChangeLabel.text      = "No subscriptions yet"
        donutChart.visibility   = View.GONE
        tvEmptyChart.visibility = View.VISIBLE
    }
    private fun buildLegendRow(color: Int, label: String, amount: Double): View {
        val dp = resources.displayMetrics.density

        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
            setPadding(0, (10 * dp).toInt(), 0, (10 * dp).toInt())
        }

        val dot = View(this).apply {
            val size = (12 * dp).toInt()
            layoutParams = LinearLayout.LayoutParams(size, size).also { it.marginEnd = (12 * dp).toInt() }
            background = ColorDrawable(color)
        }

        val tvLabel = TextView(this).apply {
            text         = label
            textSize     = 15f
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvAmount = TextView(this).apply {
            text      = "$%.2f".format(amount)
            textSize  = 15f
            typeface  = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111111"))
        }

        row.addView(dot)
        row.addView(tvLabel)
        row.addView(tvAmount)
        wrapper.addView(row)

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }
        wrapper.addView(divider)

        return wrapper
    }


    private fun buildBreakdownRow(sub: SubscriptionResponse): View {
        val dp = resources.displayMetrics.density

        val wrapper = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, (10 * dp).toInt(), 0, (10 * dp).toInt())
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity     = Gravity.CENTER_VERTICAL
        }

        val tvName = TextView(this).apply {
            text         = sub.name
            textSize     = 15f
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvCost = TextView(this).apply {
            text      = "$%.2f".format(sub.cost)
            textSize  = 15f
            typeface  = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111111"))
        }

        topRow.addView(tvName)
        topRow.addView(tvCost)

        val tvMeta = TextView(this).apply {
            text      = "${sub.category}  ·  ${sub.billing_cycle}"
            textSize  = 12f
            setTextColor(Color.parseColor("#AAAAAA"))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = (2 * dp).toInt() }
        }

        row.addView(topRow)
        row.addView(tvMeta)
        wrapper.addView(row)

        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#F0F0F0"))
        }
        wrapper.addView(divider)

        return wrapper
    }

    // just makes sure the billing cycles have normalized numbers
    internal fun normaliseToMonthly(cost: Double, billingCycle: String): Double {
        return when {
            billingCycle.contains("daily",    ignoreCase = true) -> cost * 30
            billingCycle.contains("biweekly", ignoreCase = true) -> cost * 2.17
            billingCycle.contains("weekly",   ignoreCase = true) -> cost * 4.33
            billingCycle.contains("month",    ignoreCase = true) -> cost
            billingCycle.contains("year",     ignoreCase = true) -> cost / 12
            else -> cost
        }
    }
    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
}