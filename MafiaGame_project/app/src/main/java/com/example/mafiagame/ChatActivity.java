package com.example.mafiagame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import com.airbnb.lottie.LottieAnimationView;
import com.example.mafiagame.DTO.ChatData;
import com.example.mafiagame.DTO.LobbyData;
import com.example.mafiagame.DTO.MemberData;
import com.example.mafiagame.DTO.NickJobData;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.N)
public class ChatActivity extends AppCompatActivity implements VoteDialog.SingleChoiceListener {

    //view
    private Button Button_send;
    public static String nickName;
    private EditText EditText_chat;
    private TextView TextView_roomPrsnl;
    private TextView TextView_roomName;
    private Button Button_chat_option;
    private GridView GridView_member;
    private TextView TextView_myJob;
    private TextView TextView_status;

    private Button Button_imoji1;
    private Button Button_imoji2;
    private Button Button_imoji3;
    private Button Button_imoji4;



    //recycler view
    private RecyclerView RecyclerView_chat;
    private FirebaseRecyclerAdapter<ChatData, RecyclerView.ViewHolder> firebaseRecyclerAdapter_chat;
    //grid view
    private GridAdapter gridAdapter_member;


    //key-constant 선언
    private String KEY_ROOM_NAME = MainActivity.KEY_ROOM_NAME;
    private String KEY_NICKNAME = MainActivity.KEY_NICKNAME; //쉐어드프리퍼런스 용
    private String KEY_ROOM_ID = MainActivity.KEY_ROOM_ID;

    private String KEY_BGM = MainActivity.KEY_BGM;
    private String KEY_EFCT = MainActivity.KEY_EFFECT;

    private String PREFERENCE = MainActivity.PREFERENCE;

    private String LOBBY_LIST = MainActivity.LOBBY_LIST;
    private String START_LIST = MainActivity.START_LIST;
    private String END_LIST = MainActivity.END_LIST;

    private String ROOM_INFO = MainActivity.ROOM_INFO;
    private String CHAT = MainActivity.CHAT;
    private String NICK_MEMBER = "NICK_MEMBER";
    private String GAME_MEMBER = "GAME_MEMBER";
    private final String MANAGER= "MANAGER";
    private String GAMING = "GAMING";
    private String GAME_OVER = "GAME_OVER";
    private String JOB_MAP = "JOB_MAP";
    private String DAY_VOTE_RESULT = "DAY_VOTE_RESULT";

    private final String MAFIA_WIN = "MAFIA_WIN";
    private final String CITIZEN_WIN = "CITIZEN_WIN";

    String WIN = "WIN";
    String LOOSE = "LOOSE";



    private String roomName;
    private String roomID;

    //SharedPreferences
    private SharedPreferences preferences;

    //roomList
    DatabaseReference ref_lobbyList;
    DatabaseReference ref_startList;
    DatabaseReference ref_endList;

    //ref 선언
    DatabaseReference ref_C_Chat;
    DatabaseReference ref_M_Chat;
    DatabaseReference ref_end_C_Chat;
    DatabaseReference ref_end_M_Chat;

    DatabaseReference ref_lobby_member;
    DatabaseReference ref_nickNameList;
    DatabaseReference ref_memberList;
    DatabaseReference ref_end_member;

    DatabaseReference ref_lobby_room;
    DatabaseReference ref_start_room;
    DatabaseReference ref_end_room;

    DatabaseReference ref_lobby_dto;
    DatabaseReference ref_start_dto;
    DatabaseReference ref_end_dto;

    DatabaseReference ref_onGaming;
    DatabaseReference ref_gameOver;
    DatabaseReference ref_status;
    DatabaseReference ref_manager;
    DatabaseReference ref_job;

    DatabaseReference ref_citizen_vote_map;
    DatabaseReference ref_citizen_vote_result;
    DatabaseReference ref_last_vote_map;
    DatabaseReference ref_last_vote_result;

    DatabaseReference ref_night_result;

    DatabaseReference ref_mafia_vote_map;
    DatabaseReference ref_doctor_vote;

    Query query_C_Chat;
    Query query_M_Chat;

    DialogFragment singleChoiceDialog;


    //
    ArrayList<MemberData> memberList = new ArrayList<>();
    ArrayList<String> nickNameList = new ArrayList<>(); //전체 닉네임 리스트
    ArrayList<String> aliveList = new ArrayList<>(); //투표다이얼로그에 사용

    HashMap<String,String> citizenVote_map = new HashMap<>();
    HashMap<String,String> job_map = new HashMap<>();
    HashMap<String,Boolean> lastVote_map = new HashMap<>();
    HashMap<String,String> mafiaVote_map = new HashMap<>();
    HashMap<String,Boolean> night_vote_result = new HashMap<>();

    String doctor_vote;
    boolean isStart_scroll;



    GenericTypeIndicator<ArrayList<String>> listStringGType =
            new GenericTypeIndicator<ArrayList<String>>(){};
    GenericTypeIndicator<ArrayList<MemberData>> listMemberGType =
            new GenericTypeIndicator<ArrayList<MemberData>>(){};
    GenericTypeIndicator<HashMap<String,String>> mapStringGType =
            new GenericTypeIndicator<HashMap<String,String>>(){};
    GenericTypeIndicator<HashMap<String,Boolean>> mapBooleanGType =
            new GenericTypeIndicator<HashMap<String,Boolean>>(){};
    GenericTypeIndicator<HashMap<String,Integer>> mapIntegerGType =
            new GenericTypeIndicator<HashMap<String,Integer>>(){};


    //게임 변수
    private String my_JOB;
    private boolean onGaming;
    private boolean iamLastSpeaker;
    private boolean imDead;
    private String lastSpeaker;
    private boolean managerChanged;
//    private HashMap<String,Boolean> last_vote_result;
    boolean win;
    private int mafia_cnt;
    private int citizen_cnt;

    //직업 상수
    private final String CITIZEN = "CITIZEN";
    private final String MAFIA = "MAFIA";
    private final String DOCTOR = "DOCTOR";
    private final String POLICE = "POLICE";

    private final String CITIZEN_CHAT = "CITIZEN_CHAT";
    private final String MAFIA_CHAT = "MAFIA_CHAT";

    private final String NIGHT_RESULT = "NIGHT_RESULT";

    private final String CITIZEN_VOTE = "CITIZEN_VOTE";
    private final String MAFIA_SHOT = "MAFIA_SHOT";
    private final String DOCTOR_HEAL = "DOCTOR_HEAL";
    private final String POLICE_INVEST = "POLICE_INVEST";
    private final String VICTIM = "VICTIM";

    //방장변수
    private String manager_nick;

    //Thread
    TimerThread timerThread;
    String timer_txt;

    //Timer test
    TextView textView_timer;
    Button Button_gameStart;
    DatabaseReference ref_timer;

    String TIMER = "TIMER";
    String STATUS = "STATUS";

    private final int MAX_PRSNL = 4;

    String status;

    private final String DAY_TALK = "DAY_TALK";
    private final String DAY_VOTE = "DAY_VOTE";
    private final String LAST_SPEAK = "LAST_SPEAK";
    private final String LAST_VOTE = "LAST_VOTE";
    private final String LAST_VOTE_RESULT = "LAST_VOTE_RESULT";
    private final String NIGHT_TALK = "NIGHT_TALK";
    private final String NIGHT_VOTE = "NIGHT_VOTE";



    //유저 리스너
    //타이머
    ValueEventListener timer_Listener;
    //멤버리스트 - 그리드어댑터, aliveList 용
    ValueEventListener memberList_Listener;
    //방장 교체
    ValueEventListener manager_Listener;
    //게임 진행 상태
    ValueEventListener status_Listener;
    //게임시작
    ValueEventListener onGaming_Listener;
    //게임종료
    ValueEventListener gameOver_Listener;
    //직업배정
    ValueEventListener job_map_Listener;
    //시민투표 결과
    ValueEventListener citizen_vote_result_Listener;
    //최종투표 결과
    ValueEventListener last_vote_result_Listener;
    //밤투표 결과
    ValueEventListener night_result_Listener;

    //방장 리스너
    //닉네임리스트 리스너
    ValueEventListener nickNameList_Listener;
    //시민투표
    ValueEventListener citizen_vote_map_Listener;
    //최종투표
    ValueEventListener last_vote_map_Listener;
    //마피아투표
    ValueEventListener mafia_vote_map_Listener;
    //의사투표
    ValueEventListener doctor_vote_Listener;



    @SuppressLint({"SetTextI18n", "HandlerLeak"})
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //full screen 모드
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //view 연동
        setContentView(R.layout.activity_chat);
        RecyclerView_chat   = findViewById(R.id.RecyclerView_chat);
        Button_send         = findViewById(R.id.Button_send);
        EditText_chat       = findViewById(R.id.EditText_chat);
        TextView_roomPrsnl  = findViewById(R.id.TextView_roomPrsnl);
        TextView_roomName   = findViewById(R.id.TextView_roomName);
        Button_chat_option  = findViewById(R.id.Button_chat_option);
        TextView_status     = findViewById(R.id.TextView_status);
        TextView_myJob      = findViewById(R.id.TextView_myJob);
        GridView_member     = findViewById(R.id.GridView_member);
        Button_gameStart    = findViewById(R.id.Button_gameStart);
        textView_timer      = findViewById(R.id.textView_test);

        Button_imoji1       = findViewById(R.id.Button_imoji1);
        Button_imoji2       = findViewById(R.id.Button_imoji2);
        Button_imoji3       = findViewById(R.id.Button_imoji3);
        Button_imoji4       = findViewById(R.id.Button_imoji4);

        //RecyclerView 설정
        RecyclerView_chat.setHasFixedSize(true);
        RecyclerView_chat.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        //SharedPreference 에서 닉네임 가져옴
        preferences = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
        nickName = preferences.getString(KEY_NICKNAME,"닉네임 empty");

        //intent 에서 roomID,roomName 가져옴
        Intent intent = getIntent();
        roomID   = intent.getStringExtra(KEY_ROOM_ID);
        roomName = intent.getStringExtra(KEY_ROOM_NAME);

        TextView_roomName.setText(roomName);    //방 이름 설정

        //ref,query 경로 정의
        {
            ref_lobbyList = FirebaseDatabase.getInstance()
                    .getReference(LOBBY_LIST);
            ref_startList = FirebaseDatabase.getInstance()
                    .getReference(START_LIST);
            ref_endList = FirebaseDatabase.getInstance()
                    .getReference(END_LIST);

            //lobby ref
            ref_lobby_room = ref_lobbyList
                    .child(roomID);
            ref_lobby_dto = ref_lobby_room
                    .child(ROOM_INFO);
            ref_lobby_member = ref_lobby_room
                    .child(NICK_MEMBER);

            //start ref
            ref_start_room = ref_startList
                    .child(roomID);
            ref_start_dto = ref_start_room
                    .child(ROOM_INFO);
            ref_nickNameList = ref_start_room
                    .child(NICK_MEMBER);
            ref_memberList = ref_start_room
                    .child(GAME_MEMBER);
            ref_C_Chat = ref_start_room
                    .child(CHAT)
                    .child(CITIZEN_CHAT);
            ref_M_Chat = ref_start_room
                    .child(CHAT)
                    .child(MAFIA_CHAT);

            //end ref
            ref_end_room = ref_endList
                    .child(roomID);
            ref_end_dto = ref_end_room
                    .child(ROOM_INFO);
            ref_end_member = ref_end_room
                    .child(NICK_MEMBER);
            ref_end_C_Chat = ref_end_room
                    .child(CHAT)
                    .child(CITIZEN_CHAT);
            ref_end_M_Chat = ref_end_room
                    .child(CHAT)
                    .child(MAFIA_CHAT);

            //query - start
            query_C_Chat = ref_C_Chat;
            query_M_Chat = ref_M_Chat;

            DatabaseReference ref_gameInfo = ref_start_room.child("GAME_INFO");
            ref_timer       = ref_gameInfo.child(TIMER);
            ref_status      = ref_gameInfo.child(STATUS);
            ref_manager     = ref_gameInfo.child(MANAGER);
            ref_onGaming    = ref_gameInfo.child(GAMING);
            ref_gameOver    = ref_gameInfo.child(GAME_OVER);

            ref_job = ref_start_room.child(JOB_MAP);

            DatabaseReference ref_day = ref_start_room.child("DAY");
            ref_citizen_vote_map    = ref_day.child(DAY_VOTE);
            ref_citizen_vote_result = ref_day.child(DAY_VOTE_RESULT);

            ref_last_vote_map       = ref_day.child(LAST_VOTE);
            ref_last_vote_result    = ref_day.child(LAST_VOTE_RESULT);

            DatabaseReference ref_night = ref_start_room.child("NIGHT");
            ref_night_result        = ref_night.child(NIGHT_RESULT);
            ref_mafia_vote_map      = ref_night.child(MAFIA_SHOT);
            ref_doctor_vote         = ref_night.child(DOCTOR_HEAL);
        }

        // 채팅 전송 버튼
        Button_send.setOnClickListener(view -> chat_submit());
        // 채팅 엔터 입력
        EditText_chat.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_ENTER) {
                chat_submit();
                return true;
            }return false;
        });

        //이모티콘 전송 버튼
        Button_imoji1.setOnClickListener(v -> {
            imoji_submit(1);
        });
        Button_imoji2.setOnClickListener(v -> {
            imoji_submit(2);
        });
        Button_imoji3.setOnClickListener(v -> {
            imoji_submit(3);
        });
        Button_imoji4.setOnClickListener(v -> {
            imoji_submit(4);
        });


        //채팅 리사이클러뷰
        buildFireBaseAdapter();

        //환경설정 클릭
        Button_chat_option.setOnClickListener(v -> {
            //환경설정 다이얼로그 생성
            OptionDialog optionDialog = new OptionDialog(ChatActivity.this,preferences);
            optionDialog.show_optionDialog();
        });

        //효과음 on,off 리스너
        SharedPreferences.OnSharedPreferenceChangeListener sharedListener = (sharedPreferences, key) -> {
            if (KEY_EFCT.equals(key)) {
                if (sharedPreferences.getBoolean(key, true)) {
//                    play_lobby_BGM();
                } else {
//                    stop_lobby_BGM();
                }
            }
        };

        preferences.registerOnSharedPreferenceChangeListener(sharedListener);

        gridAdapter_member = new GridAdapter(getApplicationContext(), R.layout.member_raw, memberList);
        GridView_member.setAdapter(gridAdapter_member);
    }

    @Override
    protected void onResume() {

        //타이머 리스너
        timer_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() != null){
                    timer_txt = dataSnapshot.getValue().toString();
                    textView_timer.setText(timer_txt);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        //닉네임 리스트 리스너 - 방장만 리슨하고 유저들에게는 멤버 리스트로 변환후 전달
        nickNameList_Listener = new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                ArrayList<String> nickNameList_temp = dataSnapshot.getValue(listStringGType);
                if (nickNameList_temp == null) return;

                //멤버 채워지면 자동 시작
                if (nickNameList_temp.size() >= MAX_PRSNL && !onGaming) gameStart();

                nickNameList = nickNameList_temp;

                //로비에 DTO 갱신: 로비 어댑터 인원 참조용
                //로비 prsnl 갱신
                listListener_dto_trans(ref_lobby_dto);
                listListener_dto_trans(ref_start_dto);

                //멤버 리스트 서버에 셋 - 유저의 그리드어댑터에게 전달
                //대기 중일때는 모두 생존으로 셋
                if (!onGaming){
                    ArrayList<MemberData> memberList_temp = new ArrayList<>();
                    for (String nick : nickNameList){
                        memberList_temp.add(new MemberData(nick,true));
                    }
                    ref_memberList.setValue(memberList_temp);
                }
                //게임 중에 멤버 변화 생겼을때
                //현 멤버리스트 중에서 새로운 닉네임 리스트와 중복되는 멤버만 모아 서버에 셋
                else {
                    ArrayList<MemberData> memberList_temp = new ArrayList<>();
                    for (MemberData member : memberList) {
                        if (nickNameList.contains(member.getNickName())){
                            memberList_temp.add(member);
                        }
                    }

                    ref_memberList.setValue(memberList_temp);
                }

            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        //방장이 세팅한 맵을 받아서 리스트로 변환후 그리드어댑터 셋
        //닉네임,생존여부만 가지고 있음
        memberList_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<MemberData> memberList_temp = dataSnapshot.getValue(listMemberGType);
                if (memberList_temp == null) return;

                //인원 텍스트뷰 갱신
                String memberCnt = memberList_temp.size()+"";
                TextView_roomPrsnl.setText(memberCnt);

                memberList = memberList_temp;
                gridAdapter_member.refreshAdapter(memberList);

                //aliveList 생성
                aliveList.clear();
                for (MemberData member : memberList_temp){
                    if (member.isAlive()){
                        aliveList.add(member.getNickName());
                    }
                }
                //멤버리스트, job_map 비교하여 마피아 수 갱신
                mafia_cnt = 0;
                citizen_cnt = 0;
                for (MemberData member : memberList_temp){
                    if (!member.isAlive()) continue;
                    //멤버의 생존 여부, 마피아인지 시민인지 비교하여 마피아,시민수 갱신
                    String nick = member.getNickName();
                    if (job_map.containsKey(nick)){
                        if (job_map.get(nick).equals(MAFIA)){
                            mafia_cnt++;
                        }else {
                            citizen_cnt++;
                        }
                    }
                }

                if (onGaming){
                    isGameOver();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        //방장 교체
        manager_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String manager_nick_temp = dataSnapshot.getValue(String.class);

                //널 처리
                if (manager_nick_temp == null) return;

                manager_nick = manager_nick_temp;

                //방장만 게임시작 버튼 보이게 - test
                if (nickName.equals(manager_nick)) {

                    if (!onGaming) {
                        Button_gameStart.setVisibility(View.VISIBLE); //게임 대기중에만 버튼 보임
                    } else {
                        managerChanged = true;
                        makeTimerThread();
                    }


                    //방장 리스너 작동
                    //닉네임 리스트
                    ref_nickNameList.addValueEventListener(nickNameList_Listener);
                    //시민투표
                    ref_citizen_vote_map.addValueEventListener(citizen_vote_map_Listener);
                    //최종투표
                    ref_last_vote_map.addValueEventListener(last_vote_map_Listener);
                    //의사투표
                    ref_doctor_vote.addValueEventListener(doctor_vote_Listener);
                    //마피아투표
                    ref_mafia_vote_map.addValueEventListener(mafia_vote_map_Listener);


                } else {
                    managerChanged = false;

                    Button_gameStart.setVisibility(View.INVISIBLE);

                    //방장 리스너 삭제
                    //닉네임 리스트
                    ref_nickNameList.removeEventListener(nickNameList_Listener);
                    //시민투표
                    ref_citizen_vote_map.removeEventListener(citizen_vote_map_Listener);
                    //최종투표
                    ref_last_vote_map.removeEventListener(last_vote_map_Listener);
                    //의사투표
                    ref_doctor_vote.removeEventListener(doctor_vote_Listener);
                    //마피아투표
                    ref_mafia_vote_map.removeEventListener(mafia_vote_map_Listener);

                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        //게임진행상태 리스너
        status_Listener = new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                status = dataSnapshot.getValue(String.class);
                if (status == null) return;

                TextView_status.setText(status); //상태창 업데이트

                if (imDead) return;

                switch (status){
                    case DAY_VOTE:
                        //시민 투표
                        vote_dialog_citizen();
                        break;
                    case LAST_VOTE:
                        //최종투표
                        vote_dialog_last();
                        break;
                    case NIGHT_VOTE:
                        //직업 투표
                        switch (my_JOB){
                            case MAFIA:
                                vote_dialog_mafia();
                                break;
                            case DOCTOR:
                                vote_dialog_doctor();
                                break;
                            case POLICE:
                                vote_dialog_police();
                                break;
                        }
                        break;
                }

                if (status.equals(DAY_TALK) || status.equals(NIGHT_TALK) || status.equals(LAST_SPEAK)){
                    if (singleChoiceDialog != null && singleChoiceDialog.getShowsDialog()){
                        singleChoiceDialog.dismiss();
                        singleChoiceDialog = null;
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //게임시작 flag 리스너
        onGaming_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(boolean.class) != null){
                    //게임 시작 플래그 온
                    onGaming = (boolean) dataSnapshot.getValue();
                }
                if (onGaming){
                    Thread thread = new Thread(){
                        @Override
                        public void run() {
                            isStart_scroll = true;
                            try {
                                Thread.sleep(3000);
                            } catch (InterruptedException ignored) { }
                            isStart_scroll = false;
                        }
                    };
                    thread.start();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //게임종료 flag
        gameOver_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String winner = dataSnapshot.getValue(String.class);
                if (winner == null) return;

                switch (winner){
                    case CITIZEN_WIN:
                        gameOver_dialog(true);
                        break;
                    case MAFIA_WIN:
                        gameOver_dialog(false);
                }

                if (nickName.equals(manager_nick)){
                    ref_start_room.removeValue();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //직업 배정 리스너
        job_map_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                HashMap<String,String> job_map_temp = dataSnapshot.getValue(mapStringGType);

                if (job_map_temp == null) return;
                job_map = job_map_temp;

                if (job_map.get(nickName) != null) {
                    my_JOB = job_map.get(nickName);
                    TextView_myJob.setText(my_JOB);
                }

                Toast.makeText(getApplicationContext(),"당신은 "+my_JOB+"입니다",Toast.LENGTH_SHORT).show();



                //멤버리스트, job_map 비교하여 마피아 수 갱신
                mafia_cnt = 0;
                citizen_cnt = 0;
                for (MemberData member : memberList){
                    if (!member.isAlive()) continue;
                    //멤버의 생존 여부, 마피아인지 시민인지 비교하여 마피아,시민수 갱신
                    String nick = member.getNickName();
                    if (job_map.containsKey(nick)){
                        if (job_map.get(nick).equals(MAFIA)){
                            mafia_cnt++;
                        }else {
                            citizen_cnt++;
                        }
                        Log.d("시민,마피아카운트 생성","nick:"+nick+" job"+job_map.get(nick));
                    }
                }
                Log.d("테스트","mafia_cnt: "+mafia_cnt);
                Log.d("테스트","citizen_cnt: "+citizen_cnt);

                buildFireBaseAdapter(); //게임시작 어댑터 변경
//                int position = firebaseRecyclerAdapter_chat.getItemCount(); //게임시작시 스크롤
//                RecyclerView_chat.scrollToPosition(position-1);

            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //낮투표 결과 - 유저
        citizen_vote_result_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lastSpeaker = dataSnapshot.getValue(String.class);
                if (lastSpeaker == null) return;
                //투표 결과가 자신의 닉네임일때 최후의 변론
                iamLastSpeaker = nickName.equals(lastSpeaker);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };

        //최종투표결과 - 유저
        last_vote_result_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (imDead) return;
                HashMap<String,Boolean> last_vote_result_temp = dataSnapshot.getValue(mapBooleanGType);
                if (last_vote_result_temp == null || !last_vote_result_temp.containsKey(nickName)) return;


                Boolean imDead_temp = last_vote_result_temp.get(nickName);
                if (imDead_temp != null && imDead_temp){
                    //죽음
                    imDead = true;
                    Toast.makeText(getApplicationContext(),"당신은 죽었습니다", Toast.LENGTH_SHORT).show();
                }

            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //밤 투표 결과 - 유저
        night_result_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (imDead) return;
                HashMap<String,Boolean> night_vote_result_temp = dataSnapshot.getValue(mapBooleanGType);
                if (night_vote_result_temp == null) return;

                Boolean imDead_temp = night_vote_result_temp.get(nickName);
                if (imDead_temp != null && imDead_temp){
                    //죽음
                    imDead = true;
                    Toast.makeText(getApplicationContext(),"당신은 죽었습니다", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };




        //방장
        //낮투표
        citizen_vote_map_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                citizenVote_map = dataSnapshot.getValue(mapStringGType);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //최종투표
        last_vote_map_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                lastVote_map = dataSnapshot.getValue(mapBooleanGType);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //마피아투표
        mafia_vote_map_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mafiaVote_map = dataSnapshot.getValue(mapStringGType);
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };
        //의사투표
        doctor_vote_Listener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                doctor_vote = dataSnapshot.getValue(String.class);
                if (doctor_vote == null){
                    doctor_vote = "";
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) { }
        };



        //유저 리스너 add

        //타이머
        ref_timer.addValueEventListener(timer_Listener);
        //닉,생사 리스트 - 그리드어댑터, aliveList 갱신
        ref_memberList.addValueEventListener(memberList_Listener);
        //방장 갱신
        ref_manager.addValueEventListener(manager_Listener);
        //게임진행상태
        ref_status.addValueEventListener(status_Listener);
        //게임시작 flag
        ref_onGaming.addValueEventListener(onGaming_Listener);
        //게임종료 flag
        ref_gameOver.addValueEventListener(gameOver_Listener);
        //직업 배정
        ref_job.addValueEventListener(job_map_Listener);
        //낮 결과
        ref_citizen_vote_result.addValueEventListener(citizen_vote_result_Listener);
        //최종 결과
        ref_last_vote_result.addValueEventListener(last_vote_result_Listener);
        //밤 결과
        ref_night_result.addValueEventListener(night_result_Listener);



        //방장 게임 시작 버튼 test
        Button_gameStart.setOnClickListener(v -> gameStart() );

        //입장시 매니저가 없다면 자신이 방장이 됨
        //(방 최초 생성시)
        ref_manager.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                String manager_nick_temp = mutableData.getValue(String.class);
                //현재 매니저가 널이면 자신이 매니저가 됨
                if (manager_nick_temp == null) {
                    mutableData.setValue(nickName);
                    return Transaction.success(mutableData);
                }
                return Transaction.success(mutableData);
            }
            @Override public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
        //입장시 인원 추가+1
        //멤버중복 방지
        ref_nickNameList.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                ArrayList<String> nickList = mutableData.getValue(listStringGType);
                if (nickList == null) nickList = new ArrayList<>();

                //자신의 닉네임이 없으면 닉네임 추가 (입장)
                if (!nickList.contains(nickName)) nickList.add(nickName);
                mutableData.setValue(nickList);

                return Transaction.success(mutableData);
            }

            @Override public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
        });

        super.onResume();
    }

    private void isGameOver(){
        if (manager_nick.equals(nickName)){
            if (mafia_cnt >= citizen_cnt){
                ref_gameOver.setValue(MAFIA_WIN);
                String chatTime = System.currentTimeMillis()+" ";
                ChatData chatData = new ChatData(MANAGER, "게임이 종료 되었습니다!!");
                ref_C_Chat.child(chatTime+CHAT).setValue(chatData);
                ref_M_Chat.child(chatTime+CHAT).setValue(chatData);
                if (timerThread != null) {
                    timerThread.interrupt();
                    timerThread = null;
                }

            }
            if (mafia_cnt == 0){
                ref_gameOver.setValue(CITIZEN_WIN);
                String chatTime = System.currentTimeMillis()+" ";
                ChatData chatData = new ChatData(MANAGER, "게임이 종료 되었습니다!!");
                ref_C_Chat.child(chatTime+CHAT).setValue(chatData);
                ref_M_Chat.child(chatTime+CHAT).setValue(chatData);
                if (timerThread != null) {
                    timerThread.interrupt();
                    timerThread = null;
                }
            }

        }
    }

    private void makeTimerThread(){
        //이미 작동중인 쓰레드 정지
        if (timerThread != null){ timerThread.interrupt(); }

        //타이머 쓰레드 실행
        timerThread = new TimerThread();
        timerThread.start();
    }


    //방장 게임시작 메소드
    private void gameStart(){

        //버튼 사라짐
        Button_gameStart.setVisibility(View.INVISIBLE);

        //직업 배정, 서버 전달
        setJob(nickNameList);

        ref_lobby_room.removeValue(); //게임시작시 로비에서 방 삭제
        onGaming = true;              //flag on
        ref_onGaming.setValue(true);  //서버에 게임 시작 flag set

        makeTimerThread();

    }

    //시민 투표
    public void vote_dialog_citizen(){

        singleChoiceDialog = new VoteDialog(aliveList, ref_citizen_vote_map, nickName, CITIZEN);
        singleChoiceDialog.setCancelable(false);
        FragmentManager fragmentManager = getSupportFragmentManager();
        singleChoiceDialog.show(fragmentManager, "Single Choice Dialog");
        //java.lang.IllegalStateException: Can not perform this action after onSaveInstanceState
        //현유저가(방장) 나가고 다음 쓰레드 시작후 다이얼로그 생성하려할때 오류발생
    }

    //최종 투표 - 찬성, 반대
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void vote_dialog_last(){
        singleChoiceDialog = new LastVoteDialog(ref_last_vote_map, nickName);
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    //마피아 능력
    public void vote_dialog_mafia(){
        singleChoiceDialog = new VoteDialog(aliveList, ref_mafia_vote_map, nickName, MAFIA);
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    //의사 능력
    public void vote_dialog_doctor(){
        singleChoiceDialog = new VoteDialog(aliveList, ref_doctor_vote, nickName, DOCTOR);
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    //경찰 능력
    public void vote_dialog_police(){
        singleChoiceDialog = new VoteDialog(aliveList, nickName, POLICE, job_map);
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    public void gameOver_dialog(boolean citizenWin){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.gameover_dialog, null);
        builder.setView(view).setCancelable(false);
        final ListView listview = view.findViewById(R.id.ListView_gameOver_list);
        final TextView title = view.findViewById(R.id.TextView_gameOver_title);
        ListViewAdapter listViewAdapter = new ListViewAdapter(this);


        //리스트 만들기 - 닉네임 , 마피아팀/시민팀
        ArrayList<NickJobData> nickJobList = new ArrayList<>();
        for (String nick1 : job_map.keySet()){
            nickJobList.add(new NickJobData(nick1,job_map.get(nick1)));
        }
        listViewAdapter.setList(nickJobList);
        listview.setAdapter(listViewAdapter);

        //시민 승리
        if (citizenWin) {
            if (my_JOB.equals(MAFIA)) {
                title.setText("패배");

                win = false;
            }else {
                title.setText("승리");
                win = true;
            }
        }
        //마피아 승리
        else {
            if (my_JOB.equals(MAFIA)) {
                title.setText("승리");
                win = true;
            }else {
                title.setText("패배");
                win = false;
            }
        }

        //전적저장
        MainActivity.ref_gameRecord.child(MainActivity.uid).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData mutableData) {

                //전적 map 불러오기
                HashMap<String,Integer> recordMap = mutableData.getValue(mapIntegerGType);
                if (recordMap == null) recordMap = new HashMap<>();

                //전적 map 수정
                if (win){
                    Integer win_cnt = recordMap.get(WIN);
                    if (win_cnt == null) win_cnt = 0;
                    recordMap.put(WIN,win_cnt+1);
                }else {
                    Integer loose_cnt = recordMap.get(LOOSE);
                    if (loose_cnt == null) loose_cnt = 0;
                    recordMap.put(LOOSE,loose_cnt+1);
                }

                mutableData.setValue(recordMap);

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
        });


        //다이얼로그 보여주기
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialog1 -> {
            int s = (int) System.currentTimeMillis();
            while (true){
                if ((int)System.currentTimeMillis() > s+10000){
                    break;
                }
            }
            //로비로 나가기
            dialog1.dismiss();
            Intent intent = new Intent(getApplicationContext(),LobbyActivity.class);
            startActivity(intent);
//            try {
//                Log.e("게임오버 다이얼로그","슬립 시작");
//                Thread.sleep(5000);
//            } catch (InterruptedException e) {
//            }
        });
        dialog.show();
    }

    public class ListViewAdapter extends BaseAdapter{

        private ArrayList<NickJobData> jobList;
        private Activity activity;

        // 생성할 클래스
        ListViewAdapter(Activity activity){
            this.activity = activity;
            jobList = new ArrayList<>();
        }

        public void setList(ArrayList<NickJobData> jobList) {
            this.jobList = jobList;
        }

        @Override
        public int getCount() {
            // 리스트뷰 갯수 리턴
            return jobList.size();
        }

        @Override
        public Object getItem(int position) {
            // 리스트 값 리턴
            return jobList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListViewHolder holder;
            TextView textView_gameOverNick;
            TextView textView_gameOverJob;

            // 최초 뷰 생성
            if(convertView == null) {
                LayoutInflater inflater = (LayoutInflater) activity.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                convertView = inflater.inflate(R.layout.gameover_raw, parent, false);
                textView_gameOverNick 	= convertView.findViewById(R.id.textView_gameOverNick);
                textView_gameOverJob 	= convertView.findViewById(R.id.textView_gameOverJob);

                holder = new ListViewHolder();
                holder.textView_gameOverNick = textView_gameOverNick;
                holder.textView_gameOverJob = textView_gameOverJob;

                convertView.setTag(holder);
            }
            else {
                holder = (ListViewHolder) convertView.getTag();
                textView_gameOverNick = holder.textView_gameOverNick;
                textView_gameOverJob = holder.textView_gameOverJob;
            }

            NickJobData data = jobList.get(position);
            textView_gameOverNick.setText(data.getNickName());
            textView_gameOverJob.setText(data.getJob());


            if (data.getJob().equals(MAFIA)){
                convertView.setBackground(new ColorDrawable(getResources().getColor(R.color.red)));
            }else {
                convertView.setBackground(new ColorDrawable(getResources().getColor(R.color.blue)));
            }

            return convertView;
        }

        private class ListViewHolder {
            TextView textView_gameOverNick;
            TextView textView_gameOverJob;
        }
    }

    @Override
    public void onPositiveButtonClicked(String[] list, int position) { }

    @Override
    public void onNegativeButtonClicked() { }

    //서버 채팅 참조, 리사이클러뷰 어댑터
    private void buildFireBaseAdapter() {

        //이미 실행중일때는 중지 - 게임 시작시 어댑터 교체용
        if (firebaseRecyclerAdapter_chat != null) firebaseRecyclerAdapter_chat.stopListening();

        //쿼리 배정
        Query Query_chat;
        if (my_JOB != null && my_JOB.equals(MAFIA)){
            Query_chat = query_M_Chat;
        }else {
            Query_chat = query_C_Chat;
        }

        FirebaseRecyclerOptions<ChatData> options =
                new FirebaseRecyclerOptions.Builder<ChatData>().setQuery(Query_chat, snapshot ->
                        new ChatData(
                                snapshot.getValue(ChatData.class).getNickName(),
                                snapshot.getValue(ChatData.class).getMessage(),
                                snapshot.getValue(ChatData.class).getLottieNum(),
                                snapshot.getValue(ChatData.class).isLottie()
                        )
                ).build();

        firebaseRecyclerAdapter_chat = new FirebaseRecyclerAdapter<ChatData, RecyclerView.ViewHolder>(options) {

            @Override
            public void onDataChanged() {
                super.onDataChanged();
                if (isStart_scroll){
                    RecyclerView_chat.scrollToPosition(getItemCount()-1);
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull ChatData chatData) {
                if (viewHolder instanceof StatusChat_ViewHolder) {
                    ((StatusChat_ViewHolder)viewHolder).TextView_chat_content.setText(chatData.getMessage());
                }else if (viewHolder instanceof LeftChat_ViewHolder){
                    ((LeftChat_ViewHolder)viewHolder).TextView_chat_content.setText(chatData.getMessage());
                    ((LeftChat_ViewHolder)viewHolder).TextView_chat_name.setText(chatData.getNickName());
                }else if (viewHolder instanceof RightChat_ViewHolder){
                    ((RightChat_ViewHolder)viewHolder).TextView_chat_content.setText(chatData.getMessage());
                    ((RightChat_ViewHolder)viewHolder).TextView_chat_name.setText(chatData.getNickName());
                }else if (viewHolder instanceof ImojiLeft_ViewHolder){
                    switch (chatData.getLottieNum()){
                        case 1:
                            ((ImojiLeft_ViewHolder)viewHolder).LottieAnimationView_chatLeft.setAnimation("lottie_haha.json");
                            break;
                        case 2:
                            ((ImojiLeft_ViewHolder)viewHolder).LottieAnimationView_chatLeft.setAnimation("lottie_love.json");
                            break;
                        case 3:
                            ((ImojiLeft_ViewHolder)viewHolder).LottieAnimationView_chatLeft.setAnimation("lottie_sad.json");
                            break;
                        case 4:
                            ((ImojiLeft_ViewHolder)viewHolder).LottieAnimationView_chatLeft.setAnimation("lottie_angry.json");
                    }
                    ((ImojiLeft_ViewHolder)viewHolder).LottieAnimationView_chatLeft.setRepeatCount(10);
                    ((ImojiLeft_ViewHolder)viewHolder).LottieAnimationView_chatLeft.playAnimation();
                    ((ImojiLeft_ViewHolder)viewHolder).TextView_chat_name.setText(chatData.getNickName());
                }else if (viewHolder instanceof ImojiRight_ViewHolder){
                    switch (chatData.getLottieNum()){
                        case 1:
                            ((ImojiRight_ViewHolder)viewHolder).LottieAnimationView_chatRight.setAnimation("lottie_haha.json");
                            break;
                        case 2:
                            ((ImojiRight_ViewHolder)viewHolder).LottieAnimationView_chatRight.setAnimation("lottie_love.json");
                            break;
                        case 3:
                            ((ImojiRight_ViewHolder)viewHolder).LottieAnimationView_chatRight.setAnimation("lottie_sad.json");
                            break;
                        case 4:
                            ((ImojiRight_ViewHolder)viewHolder).LottieAnimationView_chatRight.setAnimation("lottie_angry.json");
                    }
                    ((ImojiRight_ViewHolder)viewHolder).LottieAnimationView_chatRight.setRepeatCount(20);
                    ((ImojiRight_ViewHolder)viewHolder).LottieAnimationView_chatRight.playAnimation();
                    ((ImojiRight_ViewHolder)viewHolder).TextView_chat_name.setText(chatData.getNickName());
                }
            }

            @NonNull
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view;
                if (viewType == 1){
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_raw_status, parent, false);
                    return new StatusChat_ViewHolder(view);
                }else if (viewType == 2){
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_raw_right, parent, false);
                    return new RightChat_ViewHolder(view);
                }else if (viewType == 3){
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_raw_left, parent, false);
                    return new LeftChat_ViewHolder(view);
                }else if (viewType == 4){
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_raw_right_lottie, parent, false);
                    return new ImojiRight_ViewHolder(view);
                }else {
                    view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_raw_left_lottie, parent, false);
                    return new ImojiLeft_ViewHolder(view);
                }
            }

            @Override
            public int getItemViewType(int position) {
                ChatData chatData = getItem(position);
                if (chatData.getNickName().equals(MANAGER)){
                    return 1;
                }else if (chatData.getNickName().equals(nickName)){
                    if (chatData.isLottie()){
                        return 4;
                    }else {
                        return 2;
                    }
                }else {
                    if (chatData.isLottie()){
                        return 5;
                    }else {
                        return 3;
                    }
                }
            }

        };

        // 리사이클러뷰 자동 스크롤 메소드
        // 채팅 추가 리스너
        firebaseRecyclerAdapter_chat.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int position_inserted, int itemCount) {
                super.onItemRangeInserted(position_inserted, itemCount);
                //마지막에 보이는 뷰 위치 찾기
                int lastVisiblePosition = ((LinearLayoutManager) RecyclerView_chat.getLayoutManager())
                                .findLastVisibleItemPosition();
                // 마지막에서 3개 떨어진 뷰가 보여질때 가장 아래로 스크롤.
                if (lastVisiblePosition+2 >= position_inserted) {
                    RecyclerView_chat.scrollToPosition(position_inserted);
                }
            }
        });

        RecyclerView_chat.setAdapter(firebaseRecyclerAdapter_chat); //어댑터 set
        firebaseRecyclerAdapter_chat.startListening(); //채팅 어댑터 동작
    }

    private class StatusChat_ViewHolder extends RecyclerView.ViewHolder {

        private TextView TextView_chat_content;

        private StatusChat_ViewHolder(View chat_raw_status) {
            super(chat_raw_status);
            TextView_chat_content   = chat_raw_status.findViewById(R.id.TextView_chat_content);
        }
    }

    private class LeftChat_ViewHolder extends RecyclerView.ViewHolder {

        private TextView TextView_chat_content;
        private TextView TextView_chat_name;

        private LeftChat_ViewHolder(View chat_raw_left) {
            super(chat_raw_left);
            TextView_chat_content   = chat_raw_left.findViewById(R.id.TextView_chat_content);
            TextView_chat_name      = chat_raw_left.findViewById(R.id.TextView_chat_name);
        }
    }

    private class RightChat_ViewHolder extends RecyclerView.ViewHolder {

        private TextView TextView_chat_content;
        private TextView TextView_chat_name;

        private RightChat_ViewHolder(View chat_raw_right) {
            super(chat_raw_right);
            TextView_chat_content   = chat_raw_right.findViewById(R.id.TextView_chat_content);
            TextView_chat_name      = chat_raw_right.findViewById(R.id.TextView_chat_name);;
        }
    }

    private class ImojiLeft_ViewHolder extends RecyclerView.ViewHolder {

        private LottieAnimationView LottieAnimationView_chatLeft;
        private TextView TextView_chat_name;

        private ImojiLeft_ViewHolder(View chat_raw_left_lottie) {
            super(chat_raw_left_lottie);
            LottieAnimationView_chatLeft    = chat_raw_left_lottie.findViewById(R.id.LottieAnimationView_chatLeft);
            TextView_chat_name              = chat_raw_left_lottie.findViewById(R.id.TextView_chat_name);
        }
    }

    private class ImojiRight_ViewHolder extends RecyclerView.ViewHolder {

        private LottieAnimationView LottieAnimationView_chatRight;
        private TextView TextView_chat_name;

        private ImojiRight_ViewHolder(View chat_raw_right_lottie) {
            super(chat_raw_right_lottie);
            LottieAnimationView_chatRight   = chat_raw_right_lottie.findViewById(R.id.LottieAnimationView_chatRight);
            TextView_chat_name              = chat_raw_right_lottie.findViewById(R.id.TextView_chat_name);
        }
    }

    //뒤로가기 버튼 - 경고 다이얼로그
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBackPressed() {
        //패배 경고 다이얼로그
        show_warning_dialog();
    }

    private void imoji_submit(int lottieNum) {

        //죽은 사람은 채팅불가
        if (imDead) {
            return;
        }

        ChatData writeChatData = new ChatData(nickName, lottieNum);
        String chatTime = System.currentTimeMillis() + " ";

        //낮밤, 채팅제한 메소드 추가예정
        //시민, 마피아는 보는 경로가 다름
        //밤에는 마피아만 마피아경로로 채팅가능
        if (status == null) {
            //게임 시작전엔 null
            status = "LOBBY";
        }

        switch (status) {
            case "LOBBY":
                //대기중 채팅
            case DAY_TALK:
                //낮 채팅
                //모두 채팅가능
                ref_C_Chat.child(chatTime + CHAT).setValue(writeChatData);
                ref_M_Chat.child(chatTime + CHAT).setValue(writeChatData);

                ref_end_C_Chat.child(chatTime + CHAT).setValue(writeChatData);
                ref_end_M_Chat.child(chatTime + CHAT).setValue(writeChatData);
                break;

            case NIGHT_TALK:
                //마피아만 채팅가능
                if (my_JOB.equals(MAFIA)) {
                    ref_M_Chat.child(chatTime + CHAT).setValue(writeChatData);

                    ref_end_M_Chat.child(chatTime + CHAT).setValue(writeChatData);
                }
                break;

            case LAST_SPEAK:
                //최후변론 당사자만 채팅가능
                if (iamLastSpeaker) {
                    ref_C_Chat.child(chatTime + CHAT).setValue(writeChatData);
                    ref_M_Chat.child(chatTime + CHAT).setValue(writeChatData);

                    ref_end_C_Chat.child(chatTime + CHAT).setValue(writeChatData);
                    ref_end_M_Chat.child(chatTime + CHAT).setValue(writeChatData);
                }
                break;
        }

        //사용자가 채팅을 전송하면 항상 아래로 스크롤
        int lastPosition_chat = RecyclerView_chat.getAdapter().getItemCount() - 1;
        RecyclerView_chat.scrollToPosition(lastPosition_chat);

    }

    //채팅 전송 메소드
    private void chat_submit(){
        //죽은 사람은 채팅불가
        if (imDead) {
            EditText_chat.setText("");
            return;
        }

        //EditText null 인지 체크
        if (EditText_chat.getText() != null){
            String message = EditText_chat.getText().toString();
            if (!"".equals(message)) { //빈칸이 아닐때만 메시지 전송
                //ROOM_INFO 생성, 값 대입
                com.example.mafiagame.DTO.ChatData writeChatData = new ChatData(nickName,message);
                String chatTime = System.currentTimeMillis()+" ";

                //낮밤, 채팅제한 메소드 추가예정
                //시민, 마피아는 보는 경로가 다름
                //밤에는 마피아만 마피아경로로 채팅가능
                if (status == null){
                    //게임 시작전엔 null
                    status = "LOBBY";
                }
                switch (status){

                    case "LOBBY":
                        //대기중 채팅
                    case DAY_TALK:
                        //낮 채팅
                        //모두 채팅가능
                        ref_C_Chat.child(chatTime+CHAT).setValue(writeChatData);
                        ref_M_Chat.child(chatTime+CHAT).setValue(writeChatData);

                        ref_end_C_Chat.child(chatTime+CHAT).setValue(writeChatData);
                        ref_end_M_Chat.child(chatTime+CHAT).setValue(writeChatData);
                        break;

                    case NIGHT_TALK:
                        //마피아만 채팅가능
                        if (my_JOB.equals(MAFIA)){
                            ref_M_Chat.child(chatTime+CHAT).setValue(writeChatData);

                            ref_end_M_Chat.child(chatTime+CHAT).setValue(writeChatData);
                        }
                        break;

                    case LAST_SPEAK:
                        //최후변론 당사자만 채팅가능
                        if (iamLastSpeaker){
                            ref_C_Chat.child(chatTime+CHAT).setValue(writeChatData);
                            ref_M_Chat.child(chatTime+CHAT).setValue(writeChatData);

                            ref_end_C_Chat.child(chatTime+CHAT).setValue(writeChatData);
                            ref_end_M_Chat.child(chatTime+CHAT).setValue(writeChatData);
                        }
                        break;
                }
            }
        }
        //사용자가 채팅을 전송하면 항상 아래로 스크롤
        int lastPosition_chat = RecyclerView_chat.getAdapter().getItemCount()-1;
        RecyclerView_chat.scrollToPosition(lastPosition_chat);

        //EditText 비우기
        if (EditText_chat.length() > 0){
            EditText_chat.setText(null);
        }
    }

    //퇴장시 경고 다이얼로그
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void show_warning_dialog(){

        //TextView
        final TextView textView_warning = new TextView(this);
        textView_warning.setText("경기 도중 나가시게 되면 패배로 처리됩니다.");
        textView_warning.setPadding(100,50,0,0);

        //Dialog Builder 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("경고");
        builder.setView(textView_warning);
        builder.setPositiveButton("나가기",
                (dialog, which) ->
                {
                    //퇴장:
                    //멤버 0일때 방삭제
                    dialog.dismiss();

                    if(timerThread != null) timerThread.interrupt(); //타이머 쓰레드 중지

                    //모든 리스너 중지
                    ref_timer.removeEventListener(timer_Listener);
                    ref_nickNameList.removeEventListener(nickNameList_Listener);
                    ref_memberList.removeEventListener(memberList_Listener);
                    ref_manager.removeEventListener(manager_Listener);
                    ref_status.removeEventListener(status_Listener);
                    ref_onGaming.removeEventListener(onGaming_Listener);
                    ref_job.removeEventListener(job_map_Listener);
                    ref_citizen_vote_map.removeEventListener(citizen_vote_map_Listener);
                    ref_citizen_vote_result.removeEventListener(citizen_vote_result_Listener);
                    ref_last_vote_map.removeEventListener(last_vote_map_Listener);
                    ref_last_vote_result.removeEventListener(last_vote_result_Listener);
                    ref_doctor_vote.removeEventListener(doctor_vote_Listener);
                    ref_mafia_vote_map.removeEventListener(mafia_vote_map_Listener);
                    ref_night_result.removeEventListener(night_result_Listener);
                    //채팅 어탭터 정지
                    firebaseRecyclerAdapter_chat.stopListening();



                    //닉네임리스트 트랜잭션 - 자신 nickName remove 후 업데이트
                    ref_nickNameList.runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(MutableData mutableData) {

                            //data에서 현재 리스트 가져옴
                            ArrayList<String> nickNameList_tran = mutableData.getValue(listStringGType);

                            //null 처리
                            if (nickNameList_tran == null) return Transaction.success(mutableData);

                            nickNameList_tran.remove(nickName); //멤버에서 자신 제외

                            //멤버 리스트 set
                            //멤버 리스트가 비었다면 null 처리
                            if (nickNameList_tran.isEmpty()) {
                                //ref_room 이 null 이 아니면 강제 remove
                                ref_start_room.removeValue();
                                ref_lobby_room.removeValue();

                            } else {
                                mutableData.setValue(nickNameList_tran);
                            }

                            return Transaction.success(mutableData);
                        }
                        @Override public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) { }
                    });


                    exit_manager_trans(ref_manager);

                    super.onBackPressed();
                });
        //취소 버튼
        builder.setNegativeButton("취소",
                (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    //닉네임리스트 갱신시 DTO 트랜잭션 - 로비에 인원 갱신
    //닉네임리스트 트랜잭션후 연쇄적으로 실행됨
    private void listListener_dto_trans(DatabaseReference ref_dto) {


        ref_dto.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {

                //data에서 현재 리스트 가져옴
                LobbyData lobbyData = mutableData.getValue(LobbyData.class);

                //null 처리
                if (lobbyData == null || nickNameList == null) {
                    return Transaction.success(mutableData);
                }
                lobbyData.setPrsnl(nickNameList.size());

                //ref start 방 삭제
                if (nickNameList.size() <= 0) {
                    ref_lobby_room.removeValue();
                }else {
                    mutableData.setValue(lobbyData);
                }

                return Transaction.success(mutableData);
            }
            @Override public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) { }
        });
    }

    //퇴장시 방장 트랜잭션
    //방장만 실행 - 내부
    //멤버리스트에서 자신을 제외한 아무나 선택해서 트랜잭션
    private void exit_manager_trans(DatabaseReference ref_manager) {
        ref_manager.runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                String manager_nick_temp = mutableData.getValue(String.class);

                //자신이 매니저면 닉네임 리스트에서 아무나 매니저 넘겨줌
                if (nickName.equals(manager_nick_temp)){
                    for (String nick : nickNameList){
                        if (!nickName.equals(nick)){
                            mutableData.setValue(nick);
                            break;
                        }
                    }
                }

                return Transaction.success(mutableData);
            }
            @Override public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {}
        });
    }

    //직업 배정 메소드:
    //방장만 실행
    private void setJob(ArrayList<String> nickNameList){

        //테스트
        HashMap<String,String> job_map_temp = new HashMap<>();
        job_map_temp.put(nickNameList.get(0), MAFIA);
        job_map_temp.put(nickNameList.get(1), POLICE);
        job_map_temp.put(nickNameList.get(2), DOCTOR);

//        Random random = new Random();
//        HashMap<String,String> job_map = new HashMap<>();
//
//        int citizenCnt = 0;
//        int mafiaCnt = 0;

//        for (String nick : memberList){
//
//            //기존 직업배정 메소드
//            loop:
//            while (true){
//                int ranNum = random.nextInt(4);
//                switch (ranNum){
//                    case 0:
//                        //마피아 2명
//                        if (mafiaCnt < 2){
//                            job_map.put(nick,MAFIA_CHAT);
//                            mafiaCnt++;
//                            break loop;
//                        }
//                        break;
//                    case 1:
//                        //경찰
//                        if (!job_map.containsValue(POLICE_INVEST)){
//                            job_map.put(nick,POLICE_INVEST);
//                            break loop;
//                        }
//                        break;
//                    case 2:
//                        //의사
//                        if (!job_map.containsValue(DOCTOR_HEAL)){
//                            job_map.put(nick,DOCTOR_HEAL);
//                            break loop;
//                        }
//                        break;
//                    default:
//                        //시민 4명
//                        if (citizenCnt < 4){
//                            job_map.put(nick,CITIZEN_CHAT);
//                            citizenCnt++;
//                            break loop;
//                        }
//                }
//            }
//        }


        ref_job.setValue(job_map_temp);
    }


    //타이머 쓰레드
    class TimerThread extends Thread {

        @Override
        public void run() {
            setPriority(Thread.MIN_PRIORITY);
            try{
                if (status == null || status.equals("LOBBY")) status = NIGHT_VOTE;

                Log.d("TimerThread","쓰레드 최초 시작 - managerChanged: "+managerChanged);

                while (!Thread.currentThread().isInterrupted()){

                    if (managerChanged && status.equals(DAY_TALK)){
                        Log.d("TimerThread","DAY_TALK - timer_txt: "+timer_txt);
                        ref_status.setValue(DAY_TALK);
                        sleepTimer(Integer.valueOf(timer_txt));
                        managerChanged = false;
                    }else if (status.equals(NIGHT_VOTE)){
                        Log.d("TimerThread","DAY_TALK - 정상작동");
                        ref_status.setValue(DAY_TALK);
                        sleepTimer(10);
                    }

                    if (managerChanged && status.equals(DAY_VOTE)){
                        Log.d("TimerThread","DAY_VOTE - timer_txt: "+timer_txt);
                        ref_status.setValue(DAY_VOTE);
                        sleepTimer(Integer.valueOf(timer_txt));
                        set_citizen_vote_result();
                        managerChanged = false;
                    }else if (status.equals(DAY_TALK)){
                        Log.d("TimerThread","DAY_VOTE - 정상작동");
                        ref_status.setValue(DAY_VOTE);
                        sleepTimer(10);
                        set_citizen_vote_result();
                    }


                    if (managerChanged && status.equals(LAST_SPEAK)){
                        Log.d("TimerThread","LAST_SPEAK - timer_txt: "+timer_txt);
                        ref_status.setValue(LAST_SPEAK);
                        sleepTimer(Integer.valueOf(timer_txt));
                        managerChanged = false;
                    }else if (status.equals(DAY_VOTE)){
                        Log.d("TimerThread","LAST_SPEAK - 정상작동");
                        ref_status.setValue(LAST_SPEAK);
                        sleepTimer(10);
                    }

                    if (managerChanged && status.equals(LAST_VOTE)){
                        Log.d("TimerThread","LAST_VOTE - timer_txt: "+timer_txt);
                        ref_status.setValue(LAST_VOTE);
                        sleepTimer(Integer.valueOf(timer_txt));
                        set_last_vote_result();
                        managerChanged = false;
                    }else if (status.equals(LAST_SPEAK)){
                        Log.d("TimerThread","LAST_VOTE - 정상작동");
                        ref_status.setValue(LAST_VOTE);
                        sleepTimer(10);
                        set_last_vote_result();
                    }


                    if (managerChanged && status.equals(NIGHT_TALK)){
                        Log.d("TimerThread","NIGHT_TALK - timer_txt: "+timer_txt);
                        ref_status.setValue(NIGHT_TALK);
                        sleepTimer(Integer.valueOf(timer_txt));
                        managerChanged = false;
                    }else if (status.equals(LAST_VOTE)){
                        Log.d("TimerThread","NIGHT_TALK - 정상작동");
                        ref_status.setValue(NIGHT_TALK);
                        sleepTimer(10);
                    }

                    if (managerChanged && status.equals(NIGHT_VOTE)){
                        Log.d("TimerThread","NIGHT_VOTE - timer_txt: "+timer_txt);
                        ref_status.setValue(NIGHT_VOTE);
                        sleepTimer(Integer.valueOf(timer_txt));
                        set_night_vote_result();
                        managerChanged = false;
                    }else if (status.equals(NIGHT_TALK)){
                        Log.d("TimerThread","NIGHT_VOTE - 정상작동");
                        ref_status.setValue(NIGHT_VOTE);
                        sleepTimer(10);
                        set_night_vote_result();
                    }

                }

            } catch (InterruptedException ignored){}
        }

        //
        private void sleepTimer(int timer) throws InterruptedException {
            while (timer >= 0){
                ref_timer.setValue(timer);
                timer--;
                Thread.sleep(1000);
            }
        }

        //Integer 를 비교해서 높은 순으로 정렬하여 리스트로 리턴
        private String get_highestVote(final Map<String, Integer> voteMap) {
            ArrayList<String> list = new ArrayList<>(voteMap.keySet());
            Collections.sort(list, (o1, o2) -> {
                Object v1 = voteMap.get(o1);
                Object v2 = voteMap.get(o2);
                return ((Comparable) v2).compareTo(v1);
            });
//            Collections.reverse(jobList); // 주석시 오름차순
            return list.get(0);
        }

        private void set_citizen_vote_result() throws InterruptedException{

            //투표맵 리스너 딜레이를 위해 1초 슬립
            Thread.sleep(1000);

            if (citizenVote_map == null) citizenVote_map = new HashMap<>();

            //미투표자 자동투표
            if (citizenVote_map.size() <  aliveList.size()){
                Random random = new Random();
                //미투표자 수 계산
                int minus = aliveList.size() - citizenVote_map.size();
                for (int i = 0; i < minus; i++) {
                    while (true){
                        int ranNum = random.nextInt(aliveList.size());
                        //자신을 제외한 아무나에게 자동 투표
                        if (!nickName.equals(aliveList.get(ranNum))){
                            citizenVote_map.put("random",aliveList.get(ranNum));
                            break;
                        }
                    }
                }
            }

            for (String vote: citizenVote_map.values()){
                Log.d("투표 디버그","citizenVote_map 투표된 닉: "+vote);
            }


            ArrayList<String> nicks = new ArrayList<>(citizenVote_map.values());
            //투표결과 집계 - 닉네임:투표수
            HashMap<String,Integer> result_map = new HashMap<>();
            for (String nick : nicks){
                if (result_map.containsKey(nick)){
                    int cnt = result_map.get(nick);
                    cnt += 1;
                    result_map.put(nick,cnt);
                }else {
                    result_map.put(nick,1);
                }
            }
            //가장 투표를 많이 받은 닉네임 get
            String vote_result_nick = get_highestVote(result_map);
            Log.d("투표 디버그","citizenVote_map 최종 투표!!: "+vote_result_nick);

            //투표결과 서버에 set
            ref_citizen_vote_result.setValue(vote_result_nick);
            //투표맵 초기화: 중간 퇴장하는 사람은 반영하지 않기 위해
            ref_citizen_vote_map.removeValue();

            //채팅에 업로드
            ChatData citizenResult_chat = new ChatData(MANAGER,vote_result_nick+"가 처형후보로 지목 되었습니다");
            String chatTime = System.currentTimeMillis()+" ";
            ref_C_Chat.child(chatTime+CHAT).setValue(citizenResult_chat);
            ref_M_Chat.child(chatTime+CHAT).setValue(citizenResult_chat);
            ref_end_C_Chat.child(chatTime+CHAT).setValue(citizenResult_chat);
            ref_end_M_Chat.child(chatTime+CHAT).setValue(citizenResult_chat);


        }

        private void set_last_vote_result() throws InterruptedException{

            //투표맵 리스너 딜레이를 위해 1초 슬립
            Thread.sleep(1000);
            if (lastVote_map == null) lastVote_map = new HashMap<>();

            for (Boolean vote: lastVote_map.values()){
                Log.d("투표 디버그","lastVote_map 투표: "+vote);
            }


            //투표결과 집계 - 찬성,반대
            int yes = 0;
            int no = 0;
            for (boolean vote : new ArrayList<>(lastVote_map.values())){
                if (vote) {
                    yes++;
                }
                else {
                    no++;
                }
            }

            Log.d("투표 디버그","lastVote_map 중간찬반결과: yes-"+yes+" no-"+no);



            //투표 안한사람수 확인
            int minus=0;
            if (lastVote_map.size() < aliveList.size()){
                minus = aliveList.size() - lastVote_map.size();
            }
            //안한 사람수만큼 반대++
            for (int i=0;i<minus;i++){
                no++;
            }

            Log.d("투표 디버그","lastVote_map 최종찬반결과: yes-"+yes+" no-"+no);


            ChatData lastResult_chat;

            //결과맵 생성 - 처형후보:사망여부
            HashMap<String,Boolean> last_vote_set = new HashMap<>();
            if (yes > no){
                //찬성이 많아야지만 사형 - 찬반 같으면 반대
                last_vote_set.put(lastSpeaker,true);

                //채팅에 업로드
                lastResult_chat = new ChatData(MANAGER,lastSpeaker+"가 처형 되었습니다");

                //멤버리스트 갱신
                ref_memberList.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        ArrayList<MemberData> memberList_temp = mutableData.getValue(listMemberGType);
                        if (memberList_temp == null) return Transaction.success(mutableData);
                        for (MemberData member : memberList_temp){
                            if (member.getNickName().equals(lastSpeaker)){
                                member.setAlive(false);
                                break;
                            }
                        }
                        mutableData.setValue(memberList_temp);
                        return Transaction.success(mutableData);
                    }
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
                });

            } else {
                //반대가 많아 생존
                last_vote_set.put(lastSpeaker,false);
                lastResult_chat = new ChatData(MANAGER,lastSpeaker+"는 처형되지 않았습니다");
            }

            //결과맵 서버에 set
            ref_last_vote_result.setValue(last_vote_set);
            //투표맵 초기화: 중간 퇴장하는 사람은 반영하지 않기 위해
            ref_last_vote_map.removeValue();

            String chatTime = System.currentTimeMillis()+" ";
            ref_C_Chat.child(chatTime+CHAT).setValue(lastResult_chat);
            ref_M_Chat.child(chatTime+CHAT).setValue(lastResult_chat);
            ref_end_C_Chat.child(chatTime+CHAT).setValue(lastResult_chat);
            ref_end_M_Chat.child(chatTime+CHAT).setValue(lastResult_chat);
        }

        private void set_night_vote_result() throws InterruptedException{

            //투표맵 리스너 딜레이를 위해 1초 슬립
            Thread.sleep(1000);

            if (mafiaVote_map == null) mafiaVote_map = new HashMap<>();

            ArrayList<String> mafia_vote_list = new ArrayList<>(mafiaVote_map.values());

            //마피아 중 누군가 투표하지 않았을때
            //마피아 투표권 랜덤으로 선택후 맵 전달
            if (mafia_vote_list.size() <  mafia_cnt){
                Random random = new Random();
                for (int i = 0; i < mafia_cnt; i++){
                    while (true){
                        //마피아가 아닌 임의의 유저를 리스트에 세팅
                        int ranNum = random.nextInt(aliveList.size());
                        String nick = aliveList.get(ranNum);
                        if (!job_map.get(nick).equals(MAFIA)){
                            mafia_vote_list.add(nick);
                            break;
                        }
                    }
                }
            }

            for (String vote:mafia_vote_list){
                Log.d("마피아 투표 디버그","mafiaVote_map 자동투표결과 닉:"+vote);
            }
            Log.d("마피아 투표 디버그","mafiaVote_map 사이즈:"+mafia_vote_list.size()+" 마피아카운트:"+mafia_cnt);

            String nick1;
            String nick2;
            String victim;

            //마피아 두명
            if (mafia_cnt == 2){

                nick1 = mafia_vote_list.get(0);
                nick2 = mafia_vote_list.get(1);

                if (nick1.equals(nick2)){
                    victim = nick1;
                } else {
                    Random random = new Random();
                    int ranNum = random.nextInt(2);
                    if (ranNum==0){
                        victim = nick1;
                    }else {
                        victim = nick2;
                    }
                }
            }
            //마피아 한명
            else {
                victim = mafia_vote_list.get(0); //IndexOutOfBoundsException: Index: 0, Size: 0
            }


            HashMap<String,Boolean> night_result = new HashMap<>();

            ChatData nightResult_chat;

            if (victim.equals(doctor_vote)){
                //의사가 살려 생존
                night_result.put(victim,false);
                nightResult_chat = new ChatData(MANAGER,"의사의 치료로 희생자는 나오지 않았습니다");
            }else {
                //대상 죽음
                night_result.put(victim,true);
                nightResult_chat = new ChatData(MANAGER,victim+"가 마피아의 공격에 희생되었습니다");

                //멤버리스트 갱신 - 죽은사람셋
                ref_memberList.runTransaction(new Transaction.Handler() {
                    @NonNull
                    @Override
                    public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                        ArrayList<MemberData> memberList_temp = mutableData.getValue(listMemberGType);
                        if (memberList_temp == null) return Transaction.success(mutableData);
                        for (MemberData member : memberList_temp){
                            if (member.getNickName().equals(victim)){
                                member.setAlive(false);
                                break;
                            }
                        }
                        mutableData.setValue(memberList_temp);
                        return Transaction.success(mutableData);
                    }
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
                });
            }

            //채팅 리사이클러뷰에 세팅
            String chatTime = System.currentTimeMillis()+" ";
            ref_C_Chat.child(chatTime+CHAT).setValue(nightResult_chat);
            ref_M_Chat.child(chatTime+CHAT).setValue(nightResult_chat);
            ref_end_C_Chat.child(chatTime+CHAT).setValue(nightResult_chat);
            ref_end_M_Chat.child(chatTime+CHAT).setValue(nightResult_chat);
            //투표결과 서버에 set
            ref_night_result.setValue(night_result);
            //투표맵 초기화: 중간 퇴장하는 사람은 반영하지 않기 위해
            ref_mafia_vote_map.removeValue();
            ref_doctor_vote.removeValue();

            ref_citizen_vote_result.removeValue();
        }
    }


    public static class LastVoteDialog extends DialogFragment {
        private String my_nick;
        private DatabaseReference ref_last_vote_map;
        private HashMap<String,Boolean> last_vote_map;
        boolean execute = false;

        LastVoteDialog(DatabaseReference ref_last_vote_map, String my_nick) {
            this.my_nick = my_nick;
            this.ref_last_vote_map = ref_last_vote_map;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            //멤버리스트를 배열로 - 다이얼로그는 배열만 받을수있음
            final String[] memberArray = {"찬성", "반대"};

            return builder.setTitle("투표하세요")
                    .setCancelable(false)
                    .setSingleChoiceItems(memberArray, -1, (dialogInterface, i) -> {
                        //0 = 찬성
                        //1 = 반대
                        execute = i == 0;
                        ref_last_vote_map.runTransaction(new Transaction.Handler() {
                            @NonNull
                            @Override
                            public Transaction.Result doTransaction(MutableData mutableData) {
                                //data에서 현재 맵 가져옴
                                GenericTypeIndicator<HashMap<String,Boolean>> mapBooleanGType =
                                        new GenericTypeIndicator<HashMap<String,Boolean>>(){};
                                last_vote_map = mutableData.getValue(mapBooleanGType);
                                //null 처리
                                if (last_vote_map == null){
                                    last_vote_map = new HashMap<>();
                                }

                                //자신 닉네임, 투표대상 닉네임
                                last_vote_map.put(my_nick, execute);
                                mutableData.setValue(last_vote_map);

                                return Transaction.success(mutableData);
                            }
                            @Override public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) { }
                        });
                        dialogInterface.dismiss();
                    })
                    .create();
        }
    }

    class GridAdapter extends BaseAdapter {

        Context context;
        int layout;
        ArrayList<MemberData> member_list_adapter;
        LayoutInflater inf;

        GridAdapter(Context context, int layout, ArrayList<MemberData> member_list) {
            this.context = context;
            this.layout = layout;
            this.member_list_adapter = member_list;
            inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() { return member_list_adapter.size(); }

        @Override
        public Object getItem(int position) { return member_list_adapter.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        void refreshAdapter(ArrayList<MemberData> member_list){
            this.member_list_adapter = member_list;
            notifyDataSetChanged();
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view==null) view = inf.inflate(layout, null);

            TextView TextView_member = view.findViewById(R.id.TextView_member);
            view.setClickable(false);

            //멤버 겟
            MemberData member = member_list_adapter.get(position);
            //닉네임 셋
            TextView_member.setText(member.getNickName());
            //죽은 멤버라면 배경 회색
            if (!member.isAlive()){ TextView_member.setBackgroundResource(R.color.grey); }
            return view;
        }
    }

}

