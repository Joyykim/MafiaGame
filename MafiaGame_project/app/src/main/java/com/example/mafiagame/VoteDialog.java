package com.example.mafiagame;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class VoteDialog extends DialogFragment {

    //상수
    private final String CITIZEN = "CITIZEN";
    private final String MAFIA = "MAFIA";
    private final String DOCTOR = "DOCTOR";
    private final String POLICE = "POLICE";


    private int position = 0; //default selected position
    private String vote_nick;
    private String my_nick;
    private String my_job;
    private ArrayList<String> member_list;
    private DatabaseReference ref;

    private HashMap<String, String> job_map;

    private SingleChoiceListener mListener;

    private GenericTypeIndicator<HashMap<String, String>> mapStringGType =
            new GenericTypeIndicator<HashMap<String, String>>() {
            };

    //시민,마피아,의사 투표
    VoteDialog(ArrayList<String> member_list, DatabaseReference ref, String my_nick, String my_job) {
        this.member_list = member_list;
        this.ref = ref;
        this.my_nick = my_nick;
        this.my_job = my_job;
    }

    //경찰 조사
    VoteDialog(ArrayList<String> member_list, String my_nick, String my_job, HashMap<String, String> job_map) {
        this.member_list = member_list;
        this.my_nick = my_nick;
        this.my_job = my_job;
        this.job_map = job_map;
    }

    public interface SingleChoiceListener {
        void onPositiveButtonClicked(String[] list, int position);

        void onNegativeButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (SingleChoiceListener) context;
        } catch (Exception e) {
            throw new ClassCastException(getActivity().toString() + " SingleChoiceListener must implemented");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        //멤버리스트를 배열로 - 다이얼로그는 배열만 받을수있음
        final String[] itemArray = member_list.toArray(new String[0]);

        switch (my_job) {
            case CITIZEN:
                builder.setTitle("투표하세요")
                        .setCancelable(false)
                        .setSingleChoiceItems(itemArray, -1, (dialogInterface, i) -> {
                            vote_nick = itemArray[i];

                            vote_trans(vote_nick);
                            dialogInterface.dismiss();
                        });
                break;
            case MAFIA:
                builder.setTitle("밤에 살해할 사람을 고르세요")
                        .setCancelable(false)
                        .setSingleChoiceItems(itemArray, -1, (dialogInterface, i) -> {
                            vote_nick = itemArray[i];

                            vote_trans(vote_nick);
                            dialogInterface.dismiss();
                        });
                break;
            case POLICE:
                builder.setTitle("조사할 대상을 고르세요")
                        .setCancelable(false)
                        .setSingleChoiceItems(itemArray, -1, (dialogInterface, i) -> {
                            vote_nick = itemArray[i];

                            if (job_map.get(vote_nick).equals("MAFIA")) {
                                Toast.makeText(getContext(), vote_nick + "은 마피아 입니다", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), vote_nick + "은 마피아가 아닙니다", Toast.LENGTH_SHORT).show();
                            }

                            dialogInterface.dismiss();
                        });
                break;
            case DOCTOR:
                builder.setTitle("치료할 대상을 고르세요")
                        .setCancelable(false)
                        .setSingleChoiceItems(itemArray, -1, (dialogInterface, i) -> {
                            vote_nick = itemArray[i];
                            ref.setValue(vote_nick);

                            dialogInterface.dismiss();
                        });
                break;
        }

        return builder.create();
    }

    private void vote_trans(String vote_nick) {
        ref.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                //data에서 현재 리스트 가져옴
                HashMap<String, String> vote_map = mutableData.getValue(mapStringGType);

                //null 처리
                if (vote_map == null) {
                    vote_map = new HashMap<>();
                }

                //자신 닉네임, 투표대상 닉네임
                vote_map.put(my_nick, vote_nick);

                mutableData.setValue(vote_map);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
            }
        });
    }

}
