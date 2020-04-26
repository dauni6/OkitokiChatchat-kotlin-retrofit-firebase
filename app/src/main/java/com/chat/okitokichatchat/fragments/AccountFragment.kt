package com.chat.okitokichatchat.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

import com.chat.okitokichatchat.R
import com.chat.okitokichatchat.util.DATA_USERS
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.dialog_comment.*
import kotlinx.android.synthetic.main.dialog_comment.view.*
import kotlinx.android.synthetic.main.fragment_account.*

class AccountFragment : BaseFragment() {

    private var itemLayoutInflater: LayoutInflater? = null
    private var commentLayout: View? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        itemLayoutInflater = activity?.layoutInflater
        commentLayout = layoutInflater?.inflate(R.layout.dialog_comment, null)

        statusCommentButton.setOnClickListener {
            onShowDialog(it)
        }
    }

    private fun onShowDialog(view: View) {
        val builder = AlertDialog.Builder(view.context)

        builder.setView(commentLayout).setPositiveButton("확인", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                val stringObjMap = HashMap<String, Any>()
                Log.d("확인", "${commentLayout?.commentDialogET?.text}")
                stringObjMap["comment"] = commentLayout?.commentDialogET?.text.toString()
                FirebaseDatabase.getInstance().reference.child(DATA_USERS).child(userId!!)
                    .updateChildren(stringObjMap)

            }

        }).setNegativeButton("취소", object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dialog?.dismiss()
            }

        })
            .setCancelable(false)
            .show()
    }

}
