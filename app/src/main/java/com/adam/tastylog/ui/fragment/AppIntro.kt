package com.adam.tastylog.ui.fragment

import android.animation.ObjectAnimator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.adam.tastylog.R
import com.adam.tastylog.databinding.IntroAppContentBinding
import com.adam.tastylog.databinding.IntroAppDesignBinding
import com.adam.tastylog.ui.activity.MainActivity
import com.adam.tastylog.ui.fragment.AppIntro.Companion.MAX_STEP
import com.google.android.material.tabs.TabLayoutMediator

class AppIntro : Fragment() {
    private var _binding: IntroAppContentBinding? = null
    private val binding get() = _binding!!

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = IntroAppContentBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //............................................................
        binding.viewPager2.adapter = AppIntroViewPager2Adapter()

        //............................................................
        TabLayoutMediator(binding.tabLayout, binding.viewPager2) { tab, position ->
        }.attach()

        //............................................................
        binding.viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                if (position == MAX_STEP - 1) {
                    binding.btnNext.text = getString(R.string.intro_get_started) //"Get Started"
                    binding.btnNext.contentDescription = getString(R.string.intro_get_started) //"Get Started"
                    binding.btnSkip.visibility = View.GONE // Hide Skip button
                    binding.viewSkipBetweenNext.visibility = View.GONE // Hide View
                } else {
                    binding.btnNext.text = getString(R.string.intro_next) //"Next"
                    binding.btnNext.contentDescription = getString(R.string.intro_next) //"Next"
                    binding.btnSkip.visibility = View.VISIBLE // Show Skip button
                    binding.viewSkipBetweenNext.visibility = View.VISIBLE // Hide View
                }
            }
        })



        //............................................................
        binding.btnSkip.setOnClickListener {
            startMainActivity()
        }

        //............................................................
        binding.btnNext.setOnClickListener {
            if(binding.btnNext.text.toString()==getString(R.string.intro_get_started)){
                startMainActivity()
            }
            else{
                val current = (binding.viewPager2.currentItem) + 1
                binding.viewPager2.currentItem = current

                if(current>= MAX_STEP -1){
                    binding.btnNext.text                =   getString(R.string.intro_get_started)//"Get Started"
                    binding.btnNext.contentDescription  =   getString(R.string.intro_get_started)//"Get Started"

                }else{
                    binding.btnNext.text                =   getString(R.string.intro_next)//"Next"
                    binding.btnNext.contentDescription  =   getString(R.string.intro_next)//"Next"
                }
            }
        }
    }
    private fun startMainActivity() {
        val prefs = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean("hasCompletedIntro", true)
        editor.apply()

        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }


    companion object {
        const val MAX_STEP = 3
    }
}



/**-------------------------------------------------------------------*/
/**-------------------------------------------------------------------*/
class AppIntroViewPager2Adapter : RecyclerView.Adapter<PagerVH2>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerVH2 {
        return PagerVH2(
            IntroAppDesignBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }
    override fun getItemCount(): Int = MAX_STEP

    override fun onBindViewHolder(holder: PagerVH2, position: Int) = holder.itemView.run {
        with(holder) {
            if (position == 0) {
                bindingDesign.introDescription.text = context.getString(R.string.intro_description_1)
                bindingDesign.introImage.setImageResource(R.drawable.intro1)
                resizeImageView(bindingDesign.introImage, 1300,1300) // 이미지뷰 크기 조절
                animateImageView(bindingDesign.introImage)
            }
            if (position == 1) {
                bindingDesign.introTitle.text = context.getString(R.string.intro_title_2)
                bindingDesign.introDescription.text = context.getString(R.string.intro_description_2)
                bindingDesign.introImage.setImageResource(R.drawable.intro2)
                resizeImageView(bindingDesign.introImage, 1000, 1000) // 이미지뷰 크기 조절
                animateImageView(bindingDesign.introImage)
            }
            if (position == 2) {
                bindingDesign.introTitle.text = context.getString(R.string.intro_title_3)
                bindingDesign.introDescription.text = context.getString(R.string.intro_description_3)
                bindingDesign.introImage.setImageResource(R.drawable.intro3)
                resizeImageView(bindingDesign.introImage, 1200, 1200) // 이미지뷰 크기 조절
                animateImageView(bindingDesign.introImage)
            }
        }
    }

    private fun resizeImageView(imageView: ImageView, desiredWidth: Int, desiredHeight: Int) {
        val layoutParams = imageView.layoutParams
        layoutParams.width = desiredWidth
        layoutParams.height = desiredHeight
        imageView.layoutParams = layoutParams
    }
    // 이미지뷰 서서히 나타나기 함수
    private fun animateImageView(imageView: ImageView) {
        val fadeInAnimator = ObjectAnimator.ofFloat(imageView, View.ALPHA, 0f, 1f)
        fadeInAnimator.duration = 1000 // 애니메이션 지속 시간 (1초)
        fadeInAnimator.start()
    }


}
class PagerVH2(val bindingDesign: IntroAppDesignBinding) : RecyclerView.ViewHolder(bindingDesign.root)