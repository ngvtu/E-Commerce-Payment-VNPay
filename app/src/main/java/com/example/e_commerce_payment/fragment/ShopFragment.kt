package com.example.e_commerce_payment.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.e_commerce_payment.adapter.ViewPagerShopAdapter
import com.example.e_commerce_payment.api.ApiConfig
import com.example.e_commerce_payment.api.ApiService
import com.example.e_commerce_payment.databinding.FragmentShopBinding
import com.example.e_commerce_payment.models.CategoriesItems
import com.example.e_commerce_payment.models.CategoriesResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ShopFragment : Fragment() {
    private lateinit var binding: FragmentShopBinding

    private fun getListCategories(callback: (List<CategoriesItems>) -> Unit) {
        val retrofit = ApiConfig.setUpRetrofit().create(ApiService::class.java).getCategories()

        retrofit.enqueue(object : Callback<CategoriesResponse> {
            override fun onResponse(call: Call<CategoriesResponse>, response: Response<CategoriesResponse>) {
                if (response.isSuccessful) {
                    val categories = response.body()
                    val listCategories = ArrayList<CategoriesItems>()
                    categories?.let {
                        for (category in categories) {
                            listCategories.add(category)
                        }
                        Log.d("ShopFragment", "Success to get categories!")
                        callback(listCategories)
                    }
                } else {
                    Log.d("ShopFragment", "Failed to get categories")
                }
            }

            override fun onFailure(call: Call<CategoriesResponse>, t: Throwable) {
                Log.d("ShopFragment", "Failed to connect ${t.message}")
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentShopBinding.inflate(layoutInflater)
        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getListCategories { categories ->
            initViewPager(categories)
        }

        // set viewpager to tab layout
        binding.tabLayout.setupWithViewPager(binding.viewPager)
    }


    private fun initViewPager(listCategories: List<CategoriesItems>) {
        val adapter = ViewPagerShopAdapter(childFragmentManager)

        for (category in listCategories) {
            adapter.addFragment(TabFragment.newInstance(category), category.categoryName)
            Log.d("ShopFragment", "!!!!!!!!!!!" +listCategories.size)
        }

        // set adapter to ViewPager
        binding.viewPager.adapter = adapter
        adapter.notifyDataSetChanged()
    }
}