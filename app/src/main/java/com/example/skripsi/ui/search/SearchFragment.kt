package com.example.skripsi.ui.search

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skripsi.R
import com.example.skripsi.data.remote.Book
import com.example.skripsi.data.repository.Repository
import com.example.skripsi.databinding.FragmentSearchBinding
import com.example.skripsi.ui.ViewModelFactory
import com.example.skripsi.ui.bookdetail.BookDetailActivity

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchViewModel: SearchViewModel by viewModels { ViewModelFactory(Repository(requireContext())) }
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchResults()
        setupCategories()
        setupSearchButton()
        setupBackButton()
        setupObservers()

        showExploreSection()
    }

    private fun setupSearchResults() {
        binding.rvSearch.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearch.setHasFixedSize(true)
    }

    private fun setupCategories() {
        val categories = listOf(
            Category("Comp-Sci", "Computer Science"),
            Category("Science", "science"),
            Category("History",  "history"),
            Category("Finance",  "finance"),
            Category("Business",  "business"),
            Category("Politics",  "politics"),
            Category("Philosophy", "philosophy"),

        )

        categoryAdapter = CategoryAdapter(categories) { query ->
            binding.progressBar.visibility = View.VISIBLE
            showResultsSection("Results for \"$query\"")
            searchViewModel.searchBooks(query)
        }

        binding.categoriesRecyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }

    private fun setupSearchButton() {
        binding.searchButton.setOnClickListener {
            val query = binding.edSearchBook.text.toString()
            if (query.isNotBlank()) {
                binding.progressBar.visibility = View.VISIBLE
                showResultsSection("Results for \"$query\"")
                searchViewModel.searchBooks(query)
            } else {
                Toast.makeText(requireContext(), "Please enter a search query", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupBackButton() {
        binding.backButton.setOnClickListener {
            binding.edSearchBook.text?.clear()

            showExploreSection()
        }
    }

    private fun showExploreSection() {
        binding.backButton.visibility = View.GONE
        binding.searchResultsTitle.visibility = View.GONE
        binding.rvSearch.visibility = View.GONE
        binding.noResultsText.visibility = View.GONE
        binding.exploreSectionTitle.visibility = View.VISIBLE
        binding.categoriesRecyclerView.visibility = View.VISIBLE
    }

    private fun showResultsSection(title: String) {
        binding.backButton.visibility = View.VISIBLE
        binding.searchResultsTitle.visibility = View.VISIBLE
        binding.searchResultsTitle.text = title
        binding.exploreSectionTitle.visibility = View.GONE
        binding.categoriesRecyclerView.visibility = View.GONE
    }

    private fun setupObservers() {
        searchViewModel.searchResults.observe(viewLifecycleOwner) { books ->
            binding.progressBar.visibility = View.GONE

            if (books.isEmpty()) {
                binding.rvSearch.visibility = View.GONE
                binding.noResultsText.visibility = View.VISIBLE
            } else {
                binding.rvSearch.visibility = View.VISIBLE
                binding.noResultsText.visibility = View.GONE

                searchAdapter = SearchAdapter(books) { selectedBook ->
                    navigateToBookDetail(selectedBook)
                }
                binding.rvSearch.adapter = searchAdapter
            }
        }

        searchViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            binding.progressBar.visibility = View.GONE
            binding.rvSearch.visibility = View.GONE
            binding.noResultsText.visibility = View.VISIBLE
            binding.noResultsText.text = errorMessage
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToBookDetail(book: Book) {
        val intent = Intent(requireContext(), BookDetailActivity::class.java)
        intent.putExtra("book", book as java.io.Serializable)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()

        showExploreSection()

        binding.edSearchBook.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}