package id.ac.istts.dkdm.myfragment.admin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import id.ac.istts.dkdm.R
import id.ac.istts.dkdm.databinding.FragmentAdminCharityBinding
import id.ac.istts.dkdm.myadapter.RVAdminCharityAdapter
import id.ac.istts.dkdm.mydatabase.AppDatabase
import id.ac.istts.dkdm.mydatabase.CharityEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class AdminCharityFragment(
    private var db: AppDatabase,
    private val coroutine: CoroutineScope
) : Fragment() {
    private lateinit var binding: FragmentAdminCharityBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_admin_charity, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAdminCharityBinding.bind(view)

        // SET VIEW
        coroutine.launch {
            val adapterListOfCharities = RVAdminCharityAdapter(db, coroutine, db.charityDao.getAllCharity() as ArrayList<CharityEntity>)

            requireActivity().runOnUiThread {
                binding.rvAdminCharity.layoutManager = LinearLayoutManager(view.context)
                binding.rvAdminCharity.adapter = adapterListOfCharities
            }
        }
    }
}