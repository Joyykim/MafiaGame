package com.example.mafiagame;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mafiagame.DTO.LobbyData;
import com.example.mafiagame.DTO.MemberData;
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
import java.util.HashMap;
import java.util.Random;
import java.util.regex.Pattern;

@RequiresApi(api = Build.VERSION_CODES.N)
public class LobbyActivity extends AppCompatActivity {

    //view
    private TextView TextView_lobby_nickName; //프로필 닉네임
    private ImageView ImageView_lobby_profile;
    private Button Button_createRoom; //방만들기 버튼
    private Button Button_lobby_option;//옵션
    private TextView TextView_lobby_odds; //전적
    private TextView TextView_LV; //레벨

    //recycler view
    private RecyclerView RecyclerView_lobby;
    private FirebaseRecyclerAdapter<LobbyData, Lobby_ViewHolder> firebaseRecyclerAdapter_lobby;

    //key-constant
    //쉐어드 키
    private String PREFERENCE = MainActivity.PREFERENCE;

    private String KEY_ROOM_NAME = MainActivity.KEY_ROOM_NAME;
    private String KEY_ROOM_NUM = MainActivity.KEY_ROOM_NUM;
    private String KEY_NICKNAME = MainActivity.KEY_NICKNAME;
    private String KEY_ROOM_ID = MainActivity.KEY_ROOM_ID;
    private String KEY_LOBBY_LIST = MainActivity.KEY_LOBBY_LIST;
    private final String KEY_BGM = MainActivity.KEY_BGM;
    private final String KEY_EFCT = MainActivity.KEY_EFFECT;
    //파베 키
    private String LOBBY_LIST = MainActivity.LOBBY_LIST;
    private String START_LIST = MainActivity.START_LIST;
    private String END_LIST = MainActivity.END_LIST;
    private String ROOM_INFO = "ROOM_INFO";
    private String NICK_MEMBER = "NICK_MEMBER";
    private String ROOM = "ROOM";
    private String MANAGER = "MANAGER";

    private String PROFILE = "PROFILE";


    private String NICKNAME_STORAGE = MainActivity.NICKNAME_STORAGE; //로비에서 닉네임 변경: 추가 예정

    //user-data
    private String nickName;

    private String roomName = "기본방이름";
    private String roomName1 = "방이름111";
    private String roomName2 = "방이름222";
    private String roomName3 = "방이름333";

    //SharedPreferences
    private SharedPreferences preferences;


    //제네릭 변환 객체
    private GenericTypeIndicator<ArrayList<String>> listStringGType =
            new GenericTypeIndicator<ArrayList<String>>() {
            };
    GenericTypeIndicator<ArrayList<MemberData>> listMemberGType =
            new GenericTypeIndicator<ArrayList<MemberData>>() {
            };
    private GenericTypeIndicator<ArrayList<LobbyData>> listLobbydataGType =
            new GenericTypeIndicator<ArrayList<LobbyData>>() {
            };

    //정규식
    private String regexString = "^[a-zA-Z가-힣0-9]*$";

    //firebase
    private DatabaseReference ref_lobbyList = FirebaseDatabase.getInstance().getReference(LOBBY_LIST);
    private DatabaseReference ref_startList = FirebaseDatabase.getInstance().getReference(START_LIST);
    private DatabaseReference ref_endList = FirebaseDatabase.getInstance().getReference(END_LIST);

    private Query query_lobby_list = ref_lobbyList;


    MediaPlayer mediaPlayer;

    SharedPreferences.OnSharedPreferenceChangeListener sharedListener = (sharedPreferences, key) -> {
        switch (key){
            case KEY_BGM:
                if (sharedPreferences.getBoolean(key, true)) {
                    play_lobby_BGM();
                } else {
                    stop_lobby_BGM();
                }
                break;
            case KEY_EFCT:
                if (sharedPreferences.getBoolean(key, true)) {
//                    play_lobby_BGM();
                } else {
//                    stop_lobby_BGM();
                }
                break;
        }
    };

    //전적
    int level;
    Integer win_cnt;
    Integer loose_cnt;
    String WIN = "WIN";
    String LOOSE = "LOOSE";

    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN); //full screen 모드
        //view 연동
        RecyclerView_lobby = findViewById(R.id.RecyclerView_lobby);
        TextView_lobby_nickName = findViewById(R.id.TextView_lobby_nickName);
        ImageView_lobby_profile = findViewById(R.id.ImageView_lobby_profile);
        Button_createRoom = findViewById(R.id.Button_createRoom);
        Button_lobby_option = findViewById(R.id.Button_lobby_option);
        TextView_lobby_odds = findViewById(R.id.TextView_lobby_odds);
        TextView_LV = findViewById(R.id.TextView_LV);

        //RecyclerView 설정
        RecyclerView_lobby.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_lobby = new LinearLayoutManager(getApplicationContext());
        RecyclerView_lobby.setLayoutManager(layoutManager_lobby);

        //데이터 베이스 참조 - 리사이클러뷰
        lobby_fetch();

        //방만들기 클릭
        //방이름은 중복 가능함. 방번호로 채팅방 구분
        Button_createRoom.setOnClickListener((view) -> {
            //방 생성 다이얼로그
            show_CreateRoomDialog();
        });

        //환경설정 클릭
        Button_lobby_option.setOnClickListener(v -> {
            //환경설정 다이얼로그 생성
            OptionDialog optionDialog = new OptionDialog(LobbyActivity.this, preferences);
            optionDialog.show_optionDialog();
        });

        //프로필 이미지 변경 클릭
        ImageView_lobby_profile.setOnClickListener(v -> {
            ProfileDialog profileDialog = new ProfileDialog(LobbyActivity.this);
            profileDialog.show_profileDialog();
//            Intent intent = new Intent(getApplicationContext(),);
//            startActivity(intent);
        });

        //쉐어드 프리퍼런스 참조
        preferences = getSharedPreferences(PREFERENCE, MODE_PRIVATE);

        //BGM 최초 실행
        if (preferences.getBoolean(KEY_BGM, true)) {
            play_lobby_BGM();
        } else {
            stop_lobby_BGM();
        }

        //닉네임 불러오기
        nickName = preferences.getString(KEY_NICKNAME, "닉네임 비어있음");
        TextView_lobby_nickName.setText(nickName);

        //전적 불러오기
        MainActivity.ref_gameRecord.child(MainActivity.uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Integer>> mapIntegerGType =
                        new GenericTypeIndicator<HashMap<String, Integer>>() {
                        };
                HashMap<String, Integer> recordMap = dataSnapshot.getValue(mapIntegerGType);
                if (recordMap == null) {
                    recordMap = new HashMap<>();
                }

                //전적계산
                win_cnt = recordMap.get(WIN);
                loose_cnt = recordMap.get(LOOSE);

                if (win_cnt == null) win_cnt = 0;
                if (loose_cnt == null) loose_cnt = 0;

                int total = win_cnt + loose_cnt;
                double a = (double) win_cnt / (double) total;
                int odd = (int) (a * 100);

                String odds = "승 : " + win_cnt + "    패 : " + loose_cnt + "    승률 : " + odd + "%";

                TextView_lobby_odds.setText(odds);


                //레벨 선택
                if (win_cnt < 10) {
                    level = 1;
                } else if (win_cnt < 20) {
                    level = 2;
                } else if (win_cnt < 30) {
                    level = 3;
                } else {
                    level = 4;
                }
                String lvText = "Lv."+level;
                TextView_LV.setText(lvText);


                //프로필 이미지 선택
                try {
                    int profileNum = recordMap.get(PROFILE);
                    switch (profileNum){
                        case 0:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_mafia));
                            break;
                        case 1:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_doctor));
                            break;
                        case 2:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_police));
                            break;
                        case 3:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_1));
                            break;
                        case 4:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_2));
                            break;
                        case 5:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_3));
                            break;
                        case 6:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_4));
                            break;
                        case 7:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_5));
                            break;
                        case 8:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_6));
                            break;
                        case 9:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_7));
                            break;
                        case 10:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_8));
                            break;
                        case 11:
                            ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_9));
                            break;
                    }
                }catch (NullPointerException e){
                    ImageView_lobby_profile.setImageDrawable(getResources().getDrawable(R.drawable.profile_mafia));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onStart() {
        super.onStart();

        firebaseRecyclerAdapter_lobby.startListening(); //파베 어댑터 리스너 실행

        //SharedPreference 리스너 붙이기
        preferences.registerOnSharedPreferenceChangeListener(sharedListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void play_lobby_BGM() {
        //음악 재생
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(LobbyActivity.this, R.raw.mafia_lobby);
        }
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    void stop_lobby_BGM() {
        //재생중인지 검사후 음악 정지
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void lobby_fetch() {

        //Adapter 옵션 생성
        FirebaseRecyclerOptions<LobbyData> options =
                new FirebaseRecyclerOptions.Builder<LobbyData>()
                        .setQuery(query_lobby_list, (snapshot) -> {
                            //ROOM_INFO 가져오기
                            LobbyData lobbyData = snapshot.child(ROOM_INFO).getValue(LobbyData.class);
                            //쉐어드 리스트에 ROOM_INFO 추가 - 삭제예정
                            //roomList.add(lobbyData);
                            return lobbyData;
                        }).build();

        //어댑터 정의, set 어댑터
        firebaseRecyclerAdapter_lobby = new FirebaseRecyclerAdapter<LobbyData, Lobby_ViewHolder>(options) {

            ArrayList<String> nickList;

            @NonNull
            @Override
            public Lobby_ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.lobby_raw, parent, false);
                return new Lobby_ViewHolder(view);
            }

            @SuppressLint({"RtlHardcoded", "SetTextI18n"})
            @Override
            protected void onBindViewHolder(@NonNull Lobby_ViewHolder holder, final int position, @NonNull LobbyData lobbyData) {

                if (lobbyData.getRoomName() != null) {
                    //대기 상태인 방들만 출력
                    String roomName_enter = lobbyData.getRoomName();
                    String roomPrsnl_enter = lobbyData.getPrsnl() + "";
                    String roomID_enter = lobbyData.getRoomID();

                    //방이름, 인원 set
                    holder.TextView_lobby_roomName.setText(roomName_enter);
                    holder.TextView_lobby_roomPrsnl.setText(roomPrsnl_enter);

                    DatabaseReference ref_start_nickList = FirebaseDatabase.getInstance()
                            .getReference(START_LIST)
                            .child(roomID_enter)
                            .child("NICK_MEMBER");

                    //멤버 리스트 갱신 리스너
                    ref_start_nickList.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            nickList = dataSnapshot.getValue(listStringGType);
                            if (nickList == null) {
                                nickList = new ArrayList<>();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

                    //방 클릭시 입장
                    holder.ConstraintLayout_lobby.setOnClickListener(v -> {

                        Toast.makeText(getApplicationContext(), roomName_enter + " 방으로 입장", Toast.LENGTH_SHORT).show();
                        Intent enterRoom_intent = new Intent(getApplicationContext(), ChatActivity.class);

                        enterRoom_intent.putExtra(KEY_ROOM_ID, roomID_enter);
                        enterRoom_intent.putExtra(KEY_ROOM_NAME, roomName_enter);

                        startActivity(enterRoom_intent);
                    });
                }
            }
        };
        RecyclerView_lobby.setAdapter(firebaseRecyclerAdapter_lobby);

    }

    static class Lobby_ViewHolder extends RecyclerView.ViewHolder {
        TextView TextView_lobby_roomName;
        TextView TextView_lobby_roomPrsnl;
        ConstraintLayout ConstraintLayout_lobby;

        Lobby_ViewHolder(View lobby_raw) {
            super(lobby_raw);
            TextView_lobby_roomName = lobby_raw.findViewById(R.id.TextView_lobby_roomName);
            TextView_lobby_roomPrsnl = lobby_raw.findViewById(R.id.TextView_lobby_roomPrsnl);
            ConstraintLayout_lobby = lobby_raw.findViewById(R.id.ConstraintLayout_lobby);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseRecyclerAdapter_lobby.stopListening();
    }

    //방만들기 다이얼로그
    @SuppressLint("ResourceAsColor")
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void show_CreateRoomDialog() {

        //EditText 설정
        final EditText editText_roomName = new EditText(this);
        editText_roomName.setHintTextColor(R.color.grey);
        editText_roomName.setAutofillHints("방 이름 설정");
        editText_roomName.setSingleLine(true);

        //Dialog Builder 생성
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        dialogBuilder
                .setTitle("방 만들기")
                .setView(editText_roomName)
                //엔터 입력
                .setOnKeyListener((dialog, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        room_submit(editText_roomName, dialog);
                        return true;
                    }
                    return false;
                })
                //입력 버튼
                .setPositiveButton("입력",
                        (dialog, which) -> room_submit(editText_roomName, dialog))
                //취소 버튼
                .setNegativeButton("취소",
                        (dialog, which) -> dialog.dismiss())
                .show();
    }

    //방 이름 설정 버튼 클릭
    private void room_submit(EditText editText_roomName, DialogInterface dialog) {

        if (editText_roomName.getText() != null) {
            //editText 에서 텍스트 가져오기
            String temp_roomName = editText_roomName.getText().toString();
            Pattern pattern = Pattern.compile(regexString);//정규식

            //방이름이 빈칸일때
            if (temp_roomName.equals("")) {
                //방이름 랜덤으로 지정
                Random random = new Random();
                int randomInt = random.nextInt(3);
                switch (randomInt) {
                    case 0:
                        roomName = roomName1;
                        break;
                    case 1:
                        roomName = roomName2;
                        break;
                    case 2:
                        roomName = roomName3;
                        break;
                }
                Toast.makeText(getApplicationContext(), "랜덤방 이름: " + roomName, Toast.LENGTH_SHORT).show();
            }
            //빈칸X, 정규식 통과시 그대로 저장
            else if (pattern.matcher(temp_roomName).matches()) {
                roomName = editText_roomName.getText().toString();
            }
            //정규식 통과X, 빈칸X
            else {
                //방이름이 틀렸다면 다시 입력
                Toast.makeText(getApplicationContext(), "방 이름은 한글, 영문, 숫자만 입력 가능합니다" + roomName, Toast.LENGTH_SHORT).show();
                editText_roomName.setText("");
                return;
            }
        }

        Toast.makeText(getApplicationContext(), "방 이름: " + roomName, Toast.LENGTH_SHORT).show();
        //방생성 시간 입력
        String createTime_room = System.currentTimeMillis() + " ";
        //roomID 생성
        String roomID_create = createTime_room + nickName + " " + ROOM;

        //멤버 셋 경로 지정
        DatabaseReference ref_lobby_room_create = ref_lobbyList
                .child(roomID_create);
        DatabaseReference ref_start_room_create = ref_startList
                .child(roomID_create);
        DatabaseReference ref_end_room_create = ref_endList
                .child(roomID_create);

        //입장시 인원 추가+1
        //생성시엔 멤버리스트 새로 생성
        ArrayList<String> member_list_create = new ArrayList<>();
        member_list_create.add(nickName);

        //Lobby ROOM_INFO 생성
        LobbyData lobbyData_create = new LobbyData(
                roomName, //이름
                roomID_create, //ID
                member_list_create.size() //인원
        );

        //ROOM_INFO - lobby_data 넣기
        ref_lobby_room_create.child(ROOM_INFO).setValue(lobbyData_create);
        ref_start_room_create.child(ROOM_INFO).setValue(lobbyData_create);
        ref_end_room_create.child(ROOM_INFO).setValue(lobbyData_create);

        //MEMBER - member_list_adapter 넣기
        ref_start_room_create.child(NICK_MEMBER).setValue(member_list_create);
        ref_end_room_create.child(NICK_MEMBER).setValue(member_list_create);

        //방 생성, 입장
        Intent createRoom_intent = new Intent(LobbyActivity.this, ChatActivity.class);
        createRoom_intent.putExtra(KEY_ROOM_ID, roomID_create);
        createRoom_intent.putExtra(KEY_ROOM_NAME, roomName);

        //다이얼로그 삭제, Chat 액티비티 실행
        dialog.dismiss();
        startActivity(createRoom_intent);

    }

    //로비에서 뒤로가기 클릭시 앱종료
    @Override
    public void onBackPressed() {
        System.exit(0);
    }


    class ProfileDialog {

        private Context context;
        int[] imgOn = {
                R.drawable.profile_mafia, R.drawable.profile_doctor, R.drawable.profile_police,
                R.drawable.profile_1, R.drawable.profile_2, R.drawable.profile_3, R.drawable.profile_4,
                R.drawable.profile_5, R.drawable.profile_6, R.drawable.profile_7, R.drawable.profile_8,
                R.drawable.profile_9
        };

        int[] imgOff = {
                R.drawable.profile_mafia, R.drawable.profile_doctor, R.drawable.profile_police,
                R.drawable.rofile_1, R.drawable.rofile_2, R.drawable.rofile_3, R.drawable.rofile_4,
                R.drawable.rofile_5, R.drawable.rofile_6, R.drawable.rofile_7, R.drawable.rofile_8,
                R.drawable.rofile_9
        };

        ProfileDialog(Context context) {
            this.context = context;
        }

        // 호출할 다이얼로그 함수를 정의한다.
        void show_profileDialog() {

            // Dialog 객체 생성
            final Dialog dlg = new Dialog(context);
            dlg.setContentView(R.layout.profile_dialog);
            final Button Button_profileExit = dlg.findViewById(R.id.Button_profileExit);
            final GridView GridView_profile = dlg.findViewById(R.id.GridView_profile);

            ProfileAdapter adapter = new ProfileAdapter(context, R.layout.profile_raw, imgOn, imgOff, level, dlg);
            GridView_profile.setAdapter(adapter);

            //확인 클릭 - 나가기
            Button_profileExit.setOnClickListener(view1 -> dlg.dismiss());

            // 커스텀 다이얼로그를 노출한다.
            dlg.show();
        }
    }

    class ProfileAdapter extends BaseAdapter {
        Context context;
        int layout;
        int[] imgOn;
        int[] imgOff;
        LayoutInflater inf;
        int level;
        Dialog dlg;

        ProfileAdapter(Context context, int layout, int[] imgOn, int[] imgOff, int level, Dialog dig) {
            this.context = context;
            this.layout = layout;
            this.imgOn = imgOn;
            this.imgOff = imgOff;
            this.dlg = dig;
            inf = (LayoutInflater) context.getSystemService
                    (Context.LAYOUT_INFLATER_SERVICE);
            this.level = level;
        }

        @Override
        public int getCount() {
            return imgOn.length;
        }

        @Override
        public Object getItem(int position) {
            return imgOn[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null)
                convertView = inf.inflate(layout, null);
            ImageView iv = convertView.findViewById(R.id.ImageView_lobby_profile);

            boolean onImg;
            switch (level) {
                case 1:
                    if (position < 3) {
                        iv.setImageResource(imgOn[position]);
                        onImg = true;
                    } else {
                        iv.setImageResource(imgOff[position]);
                        onImg = false;
                    }
                    break;
                case 2:
                    if (position < 6) {
                        iv.setImageResource(imgOn[position]);
                        onImg = true;
                    } else {
                        iv.setImageResource(imgOff[position]);
                        onImg = false;
                    }
                    break;
                case 3:
                    if (position < 9) {
                        iv.setImageResource(imgOn[position]);
                        onImg = true;
                    } else {
                        iv.setImageResource(imgOff[position]);
                        onImg = false;
                    }
                    break;
                default:

                    iv.setImageResource(imgOn[position]);
                    onImg = true;

//                    if (position < 3){
//                        iv.setImageResource(imgOn[position]);
//                        on = true;
//                    }else if (position < 6){
//                        iv.setImageResource(imgOn[position]);
//                        on = true;
//                    }else if (position < 9){
//                        iv.setImageResource(imgOn[position]);
//                        on = true;
//                    }else {
//                        iv.setImageResource(imgOn[position]);
//                        on = true;
//                    }
            }

            if (onImg) {
                iv.setOnClickListener(v -> {
                    MainActivity.ref_gameRecord.child(MainActivity.uid).runTransaction(new Transaction.Handler() {
                        @NonNull
                        @Override
                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                            //data에서 현재 리스트 가져옴
                            GenericTypeIndicator<HashMap<String, Integer>> mapIntegerGType =
                                    new GenericTypeIndicator<HashMap<String, Integer>>() {
                                    };
                            HashMap<String,Integer> recordMap_temp = mutableData.getValue(mapIntegerGType);
                            if (recordMap_temp == null) return Transaction.success(mutableData);

                            recordMap_temp.put(PROFILE,position);

                            mutableData.setValue(recordMap_temp);

                            return Transaction.success(mutableData);
                        }

                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) { }
                    });
                    dlg.dismiss();

                });
            } else {
                iv.setOnClickListener(v -> {
                    Toast.makeText(getApplicationContext(),"승수가 부족해 이미지를 선택할 수 없습니다",Toast.LENGTH_SHORT).show();
                });
            }

            return convertView;
        }
    }
}


