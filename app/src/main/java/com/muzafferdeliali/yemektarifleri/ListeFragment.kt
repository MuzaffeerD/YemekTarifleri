package com.muzafferdeliali.yemektarifleri

//import android.content.AbstractThreadedSyncAdapter
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
//import androidx.navigation.Navigation
import java.lang.Exception
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class ListeFragment : Fragment() {

    var yemekIsmiListesi = ArrayList<String>()
    var yemekIdListesi = ArrayList<Int>()
    private lateinit var listeAdapter : ListeRecyclerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_liste, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listeAdapter = ListeRecyclerAdapter(yemekIsmiListesi,yemekIdListesi)
        RecyclerView.LayoutManager = LinearLayoutManager(context)
        RecyclerView.Adapter= listeAdapter

        sqlVeriAlma()
    }

    fun sqlVeriAlma(){
    try {

        activity?.let {
            val  database =it.openOrCreateDatabase("Yemekler", Context.MODE_PRIVATE,null)

            val cursor = database.rawQuery("SELECT * FROM Yemekler",null)
            val YemekAdiIndex = cursor.getColumnIndex("YemekAdi")
            val yemekIdIndex =cursor.getColumnIndex("id")

            yemekIsmiListesi.clear()
            yemekIdListesi.clear()

            while (cursor.moveToNext()){
                    yemekIsmiListesi.add(cursor.getString(YemekAdiIndex))
                    println(cursor.getInt(yemekIdIndex))
        }
            listeAdapter.notifyDataSetChanged()
        cursor.close()
        }
    }catch (e:Exception){

    }

    }

}