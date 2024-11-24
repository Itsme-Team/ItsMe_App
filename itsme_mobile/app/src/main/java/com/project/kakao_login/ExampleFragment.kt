package com.project.kakao_login

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

class ExampleFragment : Fragment() {

    companion object {
        private const val ARG_TITLE = "arg_title"
        private const val ARG_MESSAGE = "arg_message"

        fun newInstance(title: String, message: String): ExampleFragment {
            val fragment = ExampleFragment()
            val args = Bundle()
            args.putString(ARG_TITLE, title)
            args.putString(ARG_MESSAGE, message)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_example, container, false)

        val titleTextView: TextView = view.findViewById(R.id.titleTextView)
        val messageTextView: TextView = view.findViewById(R.id.messageTextView)

        titleTextView.text = arguments?.getString(ARG_TITLE)
        messageTextView.text = arguments?.getString(ARG_MESSAGE)

        return view
    }
}
