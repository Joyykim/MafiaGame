package com.example.mafiagame;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.regex.Pattern;


@RequiresApi(api = Build.VERSION_CODES.N)
public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    //shared preference
    public final static String PREFERENCE = "com.example.mafiagame";

    //구글 로그인
    private GoogleApiClient googleApiClient;
    private GoogleSignInOptions googleSignInOptions;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser firebaseUser;
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainActivity";

    //view
    private SignInButton SignInButton_google;
    private EditText nickname_edit;
    private Button button_submit;
    private Button logout_button;
    private ProgressBar ProgressBar_main;

    //user-data
    private String nickName;

    //key-constant 선언

    //쉐어드 키
    //쉐어드 게시물 - 삭제예정
    public final static String KEY_ROOM_NAME = "KEY_ROOM_NAME";
    public final static String KEY_ROOM_NUM = "KEY_ROOM_NUM";
    public final static String KEY_NICKNAME = "KEY_NICKNAME";
    public final static String KEY_LOBBY_LIST = "KEY_LOBBY_LIST";
    //쉐어드 환경설정
    public final static String KEY_BGM = "KEY_BGM";
    public final static String KEY_EFFECT = "KEY_EFFECT";

    //파베 경로
    public final static String LOBBY_LIST = "LIST_LOBBY"; //대기
    public final static String START_LIST = "LIST_START"; //시작
    public final static String END_LIST = "LIST_END";     //종료

    public final static String ROOM_INFO = "ROOM_INFO";
    public final static String ROOM = "ROOM";
    public final static String CHAT = "CHAT";
    public final static String DATE = "DATE";
    public final static String CITIZEN = "CITIZEN";
    public final static String MAFIA = "MAFIA";

    public final static String NICKNAME_STORAGE= "NICKNAME_STORAGE";
    public final static String KEY_ROOM_ID = "KEY_ROOM_ID";
    public final static String GAME_RECORD = "GAME_RECORD";

    public final String ANONYMOUS = "anonymous";

    //firebase
    private DatabaseReference ref_nickStorage = FirebaseDatabase.getInstance().getReference(NICKNAME_STORAGE);
    private Query query_nickStorage = ref_nickStorage;
    private HashMap<String,String> accountMap = new HashMap<>();

    public static String uid;

    public final static DatabaseReference ref_gameRecord = FirebaseDatabase.getInstance().getReference(GAME_RECORD);

    //제네릭 타입 변환용 객체
    public GenericTypeIndicator<HashMap<String,String>> mapGType = new GenericTypeIndicator<HashMap<String,String>>() {};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //View 연동
        setContentView(R.layout.activity_main);
        SignInButton_google = findViewById(R.id.SignInButton_google);
        logout_button       = findViewById(R.id.logout_button);
        button_submit       = findViewById(R.id.button_submit);
        nickname_edit       = findViewById(R.id.nickname_edit);
        ProgressBar_main    = findViewById(R.id.ProgressBar_main);

        //full screen 모드
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //구글 로그인
        //firebase 인증 객체
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //로그인 O
        if (firebaseUser != null){
            hasUser();
        }
        //로그인 안되어 있을때는 구글로그인 버튼만 visible
        else {
            SignInButton_google.setVisibility(View.VISIBLE);

            logout_button.setVisibility(View.INVISIBLE);
            button_submit.setVisibility(View.INVISIBLE);
            nickname_edit.setVisibility(View.INVISIBLE);
        }

        //구글 로그인 클릭
        SignInButton_google.setOnClickListener((view)->{
                    Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                    startActivityForResult(signInIntent, RC_SIGN_IN);
                });

        nickName = ANONYMOUS; //기본 이름은 ANONYMOUS 로 세팅

        //구글 로그인
        googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();

        //리스너 설정
        //닉네임 submit
        //버튼 클릭
        button_submit.setOnClickListener(v ->
                nickName_submit());

        //엔터 입력
        nickname_edit.setOnKeyListener((v, keyCode, event) -> {
            if(keyCode == KeyEvent.KEYCODE_ENTER) {
                nickName_submit();
                return true;
            }return false;
        });

        //로그아웃
        logout_button.setOnClickListener(v -> {
            firebaseAuth.signOut();
            Auth.GoogleSignInApi.signOut(googleApiClient);
            nickName = ANONYMOUS;
            //로그아웃 안내 토스트
            Toast.makeText(getApplicationContext(),"로그아웃 되었습니다",Toast.LENGTH_SHORT).show();

            //UI 변경
            SignInButton_google.setVisibility(View.VISIBLE);
            logout_button.setVisibility(View.INVISIBLE);
            button_submit.setVisibility(View.INVISIBLE);
            nickname_edit.setVisibility(View.INVISIBLE);
        });
    }

    private void hasUser(){

        //구글로그인 버튼 숨기기
        SignInButton_google.setVisibility(View.INVISIBLE);

        logout_button.setVisibility(View.VISIBLE);
        button_submit.setVisibility(View.VISIBLE);
        nickname_edit.setVisibility(View.VISIBLE);

        //닉네임이 있는지 검사 - 있다면 바로 로비로
        query_nickStorage.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                accountMap = dataSnapshot.getValue(mapGType);
                try {
                    uid = firebaseUser.getUid();
                    if (accountMap.containsKey(uid)){
                        //닉네임 불러오기
                        nickName = accountMap.get(uid);
                        //SharedPreferences 에디터 생성
                        SharedPreferences preferences = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString(KEY_NICKNAME, nickName); //닉네임 저장
                        editor.commit(); //에디터 수정 완료

                        //LobbyActivity 실행
                        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }catch (NullPointerException ignored){}
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        });
    }



    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(getApplicationContext(), "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) { //requestCode 확인
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            //구글 로그인 성공
            if (result.isSuccess()) {
                //계정 가져오기
                GoogleSignInAccount account = result.getSignInAccount();
                //nickName = account.getDisplayName();

                //구글,파이어베이스 연동
                firebaseAuthWithGoogle(account);
            }
            //로그인 실패
            else { Log.e(TAG, "Google Sign-In failed."); }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());
                    //로그인 실패시 Log, ToastMessage 출력
                    if (!task.isSuccessful()) {
                        Log.w(TAG, "signInWithCredential", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                    //로그인 성공시 MainActivity 재실행
                    else {
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void nickName_submit(){

        //닉네임 정규식 패턴 설정
        //한글,영문,숫자만 가능
        //정규식
        String regexString = "^[a-zA-Zㄱ-ㅎㅏ-ㅣ가-힣0-9]*$";
        Pattern pattern = Pattern.compile(regexString);

        //edit text 닉네임 길이 제한 3-10
        int nickNameLength = nickname_edit.getText().toString().length();

        if (nickNameLength >= 3){
            if (nickNameLength <= 10){
                String temp_nickName = nickname_edit.getText().toString(); //닉네임 설정
                //정규식 검사
                if (pattern.matcher(temp_nickName).matches()){

                    //파이어베이스에서 닉네임 중복 조회
                    query_nickStorage.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            accountMap = dataSnapshot.getValue(mapGType);

                            String uid = firebaseUser.getUid();

                            //map 최초생성시에만 null
                            if (accountMap == null){
                                accountMap = new HashMap<>();
                            }else {
                                //중복발생
                                if (accountMap.containsValue(temp_nickName)){
                                    Toast.makeText(getApplicationContext(),"이미 존재하는 닉네임입니다",Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }

                            //데이터베이스에 닉네임:이메일 저장
                            nickName = temp_nickName;
                            accountMap.put(uid,nickName);
                            ref_nickStorage.setValue(accountMap);

                            SharedPreferences preferences = getSharedPreferences(PREFERENCE, MODE_PRIVATE);
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putString(KEY_NICKNAME, nickName); //닉네임 저장
                            editor.commit(); //에디터 수정 완료

                            //LobbyActivity 실행
                            Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
                    });
                }else { Toast.makeText(getApplicationContext(),"닉네임은 한글, 영문, 숫자만 입력 가능합니다",Toast.LENGTH_SHORT).show(); }
            }else { Toast.makeText(getApplicationContext(),"닉네임은 10자 이하입니다",Toast.LENGTH_LONG).show(); }
        }else { Toast.makeText(getApplicationContext(),"닉네임은 3자 이상입니다",Toast.LENGTH_LONG).show(); }

        //test
        //Toast.makeText(getApplicationContext(), nickName,Toast.LENGTH_LONG).show();
    }

}
