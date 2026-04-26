package com.example.subzero

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.subzero.global.ApiCalls
import com.example.subzero.global.Utility
import com.example.subzero.network.ApiClient
import com.example.subzero.network.SubscriptionResponse
import com.example.subzero.views.DonutChartView
import com.example.subzero.views.DonutSlice
import kotlinx.coroutines.launch
import retrofit2.Response


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
    private lateinit var subscriptionsByCategoryContainer: LinearLayout
    private lateinit var filterButton: Button
    private var calls = ApiCalls()
    private val utility = Utility()
    private val selectedCategories = mutableSetOf<String>()
    private var allCategories = listOf<String>()

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
        subscriptionsByCategoryContainer = findViewById(R.id.subscriptionsByCategoryContainer)
        filterButton       = findViewById(R.id.filterButton)
        filterButton.setOnClickListener { showCategoryFilterDialog() }
    }

    // sets up click functions for navbar
    private fun setupBottomNav() {
        findViewById<LinearLayout>(R.id.navManage).setOnClickListener { navigateToDashboard() }
        findViewById<LinearLayout>(R.id.navInsights).setOnClickListener { /* already here */ }
        findViewById<LinearLayout>(R.id.navAlerts).setOnClickListener {
            navigateToAlerts()
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            navigateToProfile()
        }
    }

    // uses token for the user acc to load
    private fun loadInsights() {
        val token = SessionManager.getToken(this) ?: return

        lifecycleScope.launch {
            val subRes = calls.loadSubscriptions(this@InsightsActivity)

            // Only include subscriptions that were created in the current month or earlier.
            // Using created_at (not renewal_date) because renewal_date rolls forward to the
            // next billing cycle, so filtering on it would exclude active older subscriptions.
            val subscriptions: List<SubscriptionResponse> = (subRes ?: emptyList()).filter { sub ->
                utility.isCurrentMonthOrEarlier(sub.created_at)
            }

            allCategories = subscriptions.map { it.category }.distinct()

            if (subscriptions.isEmpty()) {
                showEmptyState()
            } else {
                renderInsights(subscriptions)
            }
        }
    }


    private fun renderInsights(subscriptions: List<SubscriptionResponse>) {
        val filteredSubs = if (selectedCategories.isEmpty()) subscriptions else subscriptions.filter { it.category in selectedCategories }
        if (filteredSubs.isEmpty()) {
            showEmptyState()
            return
        }
        // monthly total
        val monthlyTotal: Double = filteredSubs.sumOf { sub: SubscriptionResponse ->
            normaliseToMonthly(sub.cost, sub.billing_cycle)
        }
        tvMonthlyTotal.text  = "$%.2f".format(monthlyTotal)
        tvChangePercent.text = "↘ 0%"
        tvChangeLabel.text   = "Same as last month"

        // group by category
        val byCategory: Map<String, List<SubscriptionResponse>> =
            filteredSubs.groupBy { sub: SubscriptionResponse -> sub.category }

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

        // Build grouped subscriptions by category
        subscriptionsByCategoryContainer.removeAllViews()
        categoryTotals.forEachIndexed { categoryIndex: Int, (categoryName, categoryTotal) ->
            val categoryColor = sliceColors[categoryIndex % sliceColors.size]
            val categorySubscriptions = byCategory[categoryName] ?: emptyList()

            subscriptionsByCategoryContainer.addView(
                buildCategoryGroup(categoryName, categoryColor, categoryTotal, categorySubscriptions)
            )
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

    private fun showCategoryFilterDialog() {
        val checkBoxes = allCategories.map { category ->
            CheckBox(this).apply {
                text = category
                isChecked = category in selectedCategories
            }
        }
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 16, 32, 16)
            checkBoxes.forEach { addView(it) }
        }
        AlertDialog.Builder(this)
            .setTitle("Select Categories")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                selectedCategories.clear()
                checkBoxes.filter { it.isChecked }.forEach { selectedCategories.add(it.text.toString()) }
                loadInsights() // Reload to apply filter
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildCategoryGroup(
        categoryName: String,
        categoryColor: Int,
        categoryTotal: Double,
        subscriptions: List<SubscriptionResponse>
    ): View {
        val dp = resources.displayMetrics.density

        // Create a card to wrap the entire category group
        val card = androidx.cardview.widget.CardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also {
                it.bottomMargin = (12 * dp).toInt()
            }
            radius = 18 * dp
            cardElevation = 2 * dp
            setCardBackgroundColor(Color.WHITE)
        }

        val containerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding((20 * dp).toInt(), (16 * dp).toInt(), (20 * dp).toInt(), (16 * dp).toInt())
        }

        // Category header with color indicator
        val headerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.bottomMargin = (12 * dp).toInt() }
        }

        // Color dot
        val colorDot = View(this).apply {
            layoutParams = LinearLayout.LayoutParams((12 * dp).toInt(), (12 * dp).toInt()).also {
                it.marginEnd = (12 * dp).toInt()
            }
            background = ColorDrawable(categoryColor)
        }

        // Category name
        val tvCategoryName = TextView(this).apply {
            text = categoryName
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#222222"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        // Category total
        val tvCategoryTotal = TextView(this).apply {
            text = "$%.2f".format(categoryTotal)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#111111"))
        }

        headerLayout.addView(colorDot)
        headerLayout.addView(tvCategoryName)
        headerLayout.addView(tvCategoryTotal)
        containerLayout.addView(headerLayout)

        // Add subscriptions for this category
        subscriptions.forEach { sub ->
            containerLayout.addView(buildCategorySubscriptionRow(sub, dp))
        }

        card.addView(containerLayout)
        return card
    }

    private fun buildCategorySubscriptionRow(sub: SubscriptionResponse, dp: Float): View {
        val wrapper = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).also { it.topMargin = (8 * dp).toInt() }
        }

        // Indent to show it's a child of category
        val leftPadding = (24 * dp).toInt()
        row.setPadding(leftPadding, (8 * dp).toInt(), 0, (8 * dp).toInt())

        val tvName = TextView(this).apply {
            text = sub.name
            textSize = 14f
            setTextColor(Color.parseColor("#555555"))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val tvMonthlyPrice = TextView(this).apply {
            text = "$%.2f/mo".format(normaliseToMonthly(sub.cost, sub.billing_cycle))
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(Color.parseColor("#333333"))
        }

        row.addView(tvName)
        row.addView(tvMonthlyPrice)
        wrapper.addView(row)

        return wrapper
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

    internal fun normaliseToMonthly(cost: Double, billingCycle: String): Double =
        utility.normaliseToMonthly(cost, billingCycle)
    private fun navigateToProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }
    private fun navigateToAlerts() {
        startActivity(Intent(this, AlertsActivity::class.java))
    }
    private fun navigateToDashboard() {
        startActivity(Intent(this, DashboardActivity::class.java))
    }

}
