package com.example.datto

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.MemoryResponse
import com.example.datto.GlobalVariable.GlobalVariable
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.squareup.picasso.Picasso

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MemoryView.newInstance] factory method to
 * create an instance of this fragment.
 */
class MemoryView : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private val imageView: ImageView by lazy { requireView().findViewById(R.id.memoryViewImageView) }
    private val infoText: TextView by lazy { requireView().findViewById(R.id.memoryViewMemoryNameTextView) }
    private var layoutParams = CoordinatorLayout.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        configTopAppBar()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? { // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_memory_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        APIService().doGet<MemoryResponse>(
            "groups/${arguments?.getString("groupId")}/memories/${
                arguments?.getString(
                    "id"
                )
            }", object :
                APICallback<Any> {
                override fun onSuccess(data: Any) {
                    val memory = data as MemoryResponse
                    infoText.text = memory.info
                    Picasso.get().load(GlobalVariable.BASE_URL + "files/" + memory.thumbnail)
                        .into(imageView)
                }

                override fun onError(error: Throwable) {
                    Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                }
            })

        // val imgUrl = arguments?.getString("imgUrl")
        // Picasso.get().load(imgUrl).into(imageView)
    }

    override fun onStart() {
        super.onStart()
        configTopAppBar()
    }

    override fun onResume() {
        super.onResume()
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

        main.invalidate()
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
        main.invalidate()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MemoryView.
         */ // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) = MemoryView().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}