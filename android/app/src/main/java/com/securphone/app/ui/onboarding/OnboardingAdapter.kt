package com.securphone.app.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.securphone.app.R
import com.securphone.app.databinding.ItemOnboardingPageBinding

class OnboardingAdapter : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private val titles = arrayOf(
        "Encrypted Defenses",
        "Physical Guard",
        "Immediate Distress"
    )

    private val descriptions = arrayOf(
        "Safeguard confidential network coordinates and protect identity tokens.",
        "Detect pocket removals, charger pulls, and unauthorized USB cables.",
        "Trigger remote lockdowns or transmit coordinates instantly during distress."
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingPageBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OnboardingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(titles[position], descriptions[position])
    }

    override fun getItemCount(): Int = titles.size

    class OnboardingViewHolder(private val binding: ItemOnboardingPageBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(title: String, desc: String) {
            binding.tvTitle.text = title
            binding.tvDescription.text = desc
            binding.ivImage.setImageResource(R.drawable.ic_shield)
        }
    }
}
