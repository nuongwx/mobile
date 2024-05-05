package com.example.datto

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.EventResponse
import com.example.datto.DataClass.MemoryResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.example.datto.databinding.FragmentMemoriesBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.annotations.SerializedName
import com.squareup.picasso.Picasso


// wdyk, current data object for this response does not match the actual response
data class YAGR(
    @SerializedName("_id") val id: String, val events: List<EventResponse>
)


class MemoryViewAdapter(
    private val memoryIdList: List<Pair<String, String>>, private val activity: Activity
) :
    RecyclerView.Adapter<MemoryViewAdapter.MemoryViewHolder>() {

    inner class MemoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memoryName: TextView = itemView.findViewById(R.id.memoryViewMemoryNameTextView)
        val memoryImage: ImageView = itemView.findViewById(R.id.memoryViewImageView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_memory_view, parent, false)
        return MemoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MemoryViewHolder, position: Int) {
        val currentItem = memoryIdList[position]
        val ctx = holder.itemView.context

        // Get the screen height
        val displayMetrics = DisplayMetrics()
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenHeight = displayMetrics.heightPixels

        // Set the ImageView height to the screen height
        holder.memoryImage.layoutParams.height = screenHeight + 64

        APIService(ctx).doGet<MemoryResponse>(
            "memories/${currentItem.second}",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as MemoryResponse
                    holder.memoryName.text = data.info

                    if (data.thumbnail == "") {
                        Picasso.get().load(R.drawable.cover).into(holder.memoryImage)
                        return
                    }

                    Picasso.get().load(GlobalVariable.BASE_URL + "files/" + data.thumbnail)
                        .into(holder.memoryImage)
                }

                override fun onError(error: Throwable) {
                    Toast.makeText(ctx, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }

    override fun getItemCount() = memoryIdList.size
}

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class Memories : Fragment() {
    private val hideHandler = Handler(Looper.myLooper()!!)

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var dummyButton: Button? = null
    private var fullscreenContent: View? = null
    private var fullscreenContentControls: View? = null
    private var appBar: MaterialToolbar? = null
    private var recyclerView: RecyclerView? = null

    private var layoutParams: CoordinatorLayout.LayoutParams? = null

    private var _binding: FragmentMemoriesBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private fun configAppBar() {
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        appBar.title = "Memories"
        appBar.navigationIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_back)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMemoriesBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        configAppBar()

        visible = true

        dummyButton = binding.dummyButton
        fullscreenContent = binding.fullscreenContent
        fullscreenContentControls = binding.fullscreenContentControls

        appBar = requireActivity().findViewById(R.id.app_top_app_bar)

        recyclerView = requireActivity().findViewById(R.id.swipeRight)
        recyclerView?.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(
            requireContext(), RecyclerView.HORIZONTAL, false
        )
        recyclerView?.setHasFixedSize(true)

        var adapter = MemoryViewAdapter(listOf(), requireActivity())
        recyclerView?.adapter = adapter

        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener {
            toggle()
        }

        val gestureDetector =
            GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    toggle()
                    return true
                }
            })

        recyclerView?.addOnItemTouchListener(object : RecyclerView.SimpleOnItemTouchListener() {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(e)
                return super.onInterceptTouchEvent(rv, e)
            }
        })

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        dummyButton?.setOnTouchListener(delayHideTouchListener)

        APIService(requireContext()).doGet<List<YAGR>>(
            "accounts/${CredentialService().get()}/groups",
            object : APICallback<Any> {
                override fun onSuccess(data: Any) {
                    data as List<YAGR>
                    val memoryIdList = ArrayList<Pair<String, String>>()
                    for (group in data) {
                        for (event in group.events) {
                            if (event.memory != null) {
                                memoryIdList.add(Pair(group.id, event.memory))
                            }
                        }
                    }

                    if (memoryIdList.isEmpty()) {
                        recyclerView?.visibility = View.GONE
                        fullscreenContent?.visibility = View.VISIBLE
                    } else {
                        recyclerView?.visibility = View.VISIBLE
                        fullscreenContent?.visibility = View.GONE
                        adapter = MemoryViewAdapter(memoryIdList, requireActivity())
                        recyclerView?.adapter = adapter
                        adapter.notifyDataSetChanged()
                    }
                }

                override fun onError(error: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${error.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    override fun onResume() {
        super.onResume()
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)

        configTopAppBar()

        // Hide the bottom navigation bar
        setDefaultLayout(false)
    }

    override fun onPause() {
        super.onPause()
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        dummyButton = null
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        appBar?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        appBar?.visibility = View.VISIBLE
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 1000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 0
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onStart() {
        super.onStart()
        configTopAppBar()
    }

    override fun onStop() {
        super.onStop()
        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        appBar.navigationIcon?.setTintList(null)

        val appBarLayout = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        val scrollView = requireActivity().findViewById<View>(R.id.app_scroll_view)
        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val main = requireActivity().findViewById<CoordinatorLayout>(R.id.main)
        if (main.getChildAt(1) !is RelativeLayout) {
            return
        }
        val relativeLayout = main.getChildAt(1) as RelativeLayout

        main.removeView(relativeLayout)
        relativeLayout.removeAllViews()

        main.addView(appBarLayout, 0)
        main.addView(scrollView, 1)
        bottomNavigationView.visibility = View.VISIBLE

        // hours spent on this 1 line: 3
        // https://stackoverflow.com/a/31005613
        // set the old layout params back 😭
        scrollView.layoutParams = layoutParams

        // scrollView.layoutParams = CoordinatorLayout.LayoutParams(
        //     ViewGroup.LayoutParams.MATCH_PARENT,
        //     ViewGroup.LayoutParams.MATCH_PARENT
        // )
        // (scrollView.layoutParams as CoordinatorLayout.LayoutParams).behavior = AppBarLayout.ScrollingViewBehavior()

        val topMargin = (32 * resources.displayMetrics.density).toInt()
        (main.layoutParams as ViewGroup.MarginLayoutParams).topMargin = topMargin
        main.invalidate()
    }

    private fun setDefaultLayout(viewBottomNav: Boolean = true) {
        val scrollView = requireActivity().findViewById<View>(R.id.app_scroll_view)
        val bottomNavigation =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)

        val layoutParams = scrollView.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.setMargins(
            layoutParams.leftMargin,
            layoutParams.topMargin,
            layoutParams.rightMargin,
            if (viewBottomNav) resources.getDimensionPixelSize(R.dimen.bottom_navigation_height) else 0
        )
        scrollView.layoutParams = layoutParams
        bottomNavigation.visibility = if (viewBottomNav) View.VISIBLE else View.GONE
    }

    private fun configTopAppBar() {
        val main = requireActivity().findViewById<CoordinatorLayout>(R.id.main)
        val appBarLayout = requireActivity().findViewById<AppBarLayout>(R.id.appBarLayout)
        val scrollView = requireActivity().findViewById<View>(R.id.app_scroll_view)
        val relativeLayout = RelativeLayout(context)
        relativeLayout.layoutParams = CoordinatorLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
        )

        // get a independent copy of the scrollview layout params
        if (scrollView.layoutParams is CoordinatorLayout.LayoutParams) {
            layoutParams = scrollView.layoutParams as CoordinatorLayout.LayoutParams
            scrollView.layoutParams = layoutParams
        }

        val appBar = requireActivity().findViewById<MaterialToolbar>(R.id.app_top_app_bar)
        appBar.navigationIcon = ResourcesCompat.getDrawable(resources, R.drawable.ic_back, null)
            .apply { this?.setTint(resources.getColor(R.color.md_theme_onPrimary, null)) }
        appBar.title = ""
        appBar.menu.findItem(R.id.edit).isVisible = false

        // set transparent background
        appBar.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
        appBarLayout.setBackgroundColor(resources.getColor(android.R.color.transparent, null))

        if (appBarLayout.parent is RelativeLayout) {
            return
        }

        // remove parent from appbar and scrollbar
        main.removeView(appBarLayout)
        main.removeView(scrollView)

        val bottomNavigationView =
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNavigationView.visibility = View.GONE

        relativeLayout.addView(scrollView, 0)
        relativeLayout.addView(appBarLayout, 1)

        // remove old components and add new layout
        main.addView(relativeLayout, 1)
        (main.layoutParams as ViewGroup.MarginLayoutParams).topMargin = 0
        main.invalidate()
    }
}