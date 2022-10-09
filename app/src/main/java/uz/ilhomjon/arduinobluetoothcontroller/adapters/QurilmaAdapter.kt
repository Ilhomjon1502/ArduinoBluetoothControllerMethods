package uz.ilhomjon.arduinobluetoothcontroller.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import uz.ilhomjon.arduinobluetoothcontroller.databinding.ItemRvDialogBinding
import uz.ilhomjon.arduinobluetoothcontroller.models.Qurilma

class QurilmaAdapter(val list:List<Qurilma>, val rvClick: RvClick): RecyclerView.Adapter<QurilmaAdapter.Vh>() {

    inner class Vh(var itemRvBinding: ItemRvDialogBinding):RecyclerView.ViewHolder(itemRvBinding.root){

        fun onBind(qurilma: Qurilma, position: Int){
            itemRvBinding.tvName.text = qurilma.name
            itemRvBinding.tvAddress.text = qurilma.address
            itemRvBinding.root.setOnClickListener {
                rvClick.onClick(qurilma)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Vh {
        return Vh(ItemRvDialogBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: Vh, position: Int) {
        holder.onBind(list[position], position)
    }

    override fun getItemCount(): Int = list.size
}

interface RvClick{
    fun onClick(qurilma: Qurilma)
}