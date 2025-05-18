package com.example.historymapapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TutorialAdapter(private val slides: List<TutorialSlide>)
    : RecyclerView.Adapter<TutorialAdapter.TutorialViewHolder>() {

    inner class TutorialViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView = view.findViewById(R.id.tutorialImage)
        val title: TextView  = view.findViewById(R.id.tutorialTitle)
        val desc : TextView  = view.findViewById(R.id.tutorialDesc)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val view = LayoutInflater
            .from(parent.context)
            .inflate(R.layout.tutorial_slide, parent, /*attachToRoot =*/ false)
        return TutorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        val slide = slides[position]
        holder.image.setImageResource(slide.imageRes)
        holder.title.text = slide.title
        holder.desc.text  = slide.description
    }

    override fun getItemCount(): Int = slides.size
}

private fun ImageView.setImageResource(imageRes: String) {

}
