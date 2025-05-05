package com.example.skripsi.ui.home

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.skripsi.R
import com.example.skripsi.databinding.FragmentHomeBinding
import com.example.skripsi.ui.tracking.currentlyreading.CurrentlyReadingActivity
import com.example.skripsi.ui.tracking.finishedreading.FinishedReadingActivity
import com.example.skripsi.ui.tracking.wanttoread.WantToReadActivity
import androidx.lifecycle.ViewModelProvider
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.ui.ViewModelFactory
import com.example.skripsi.ui.bookdetail.BookDetailActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var homeAdapter: HomeAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = Repository(requireContext())
        val factory = ViewModelFactory(repository)
        homeViewModel = ViewModelProvider(this, factory).get(HomeViewModel::class.java)

        val layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns
        binding.homeRecyclerView.layoutManager = layoutManager
        binding.homeRecyclerView.setHasFixedSize(true)

        homeViewModel.recentBooks.observe(viewLifecycleOwner) { books ->
            homeAdapter = HomeAdapter(books) { selectedBook ->
                val intent = Intent(requireContext(), BookDetailActivity::class.java)
                intent.putExtra("book", selectedBook)
                startActivity(intent)
            }
            binding.homeRecyclerView.adapter = homeAdapter
        }



        binding.buttonCurrentlyReading.setOnClickListener {
            val intent = Intent(requireContext(), CurrentlyReadingActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.buttonWantToRead.setOnClickListener {
            val intent = Intent(requireContext(), WantToReadActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.buttonFinishedReading.setOnClickListener {
            val intent = Intent(requireContext(), FinishedReadingActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}