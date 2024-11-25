package com.project.kakao_login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

class ExampleFragment : Fragment() {

    companion object {
        private const val ARG_IMAGE_RES = "image_res"

        fun newInstance(imageRes: Int): ExampleFragment {
            val fragment = ExampleFragment()
            val args = Bundle()
            args.putInt(ARG_IMAGE_RES, imageRes)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_example, container, false)
        val imageView : ImageView = view.findViewById(R.id.imageView)

        // 전달받은 이미지 리소스를 설정
        val imageRes = arguments?.getInt(ARG_IMAGE_RES)
        imageRes?.let{
            imageView.setImageResource(it)
        }

        return view
    }
}
