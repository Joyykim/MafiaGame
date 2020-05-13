package com.example.mafiagame;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class OptionDialog {

    private Context context;
    private SharedPreferences preferences;

    //key constant
    private String KEY_BGM = MainActivity.KEY_BGM;
    private String KEY_EFFECT = MainActivity.KEY_EFFECT;


    public OptionDialog(Context context, SharedPreferences preferences) {
        this.context = context;
        this.preferences = preferences;
    }

    // 호출할 다이얼로그 함수를 정의한다.
    public void show_optionDialog() {

        // Dialog 객체 생성
        final Dialog dlg = new Dialog(context);

        // 커스텀 다이얼로그의 레이아웃을 설정한다.
        dlg.setContentView(R.layout.option_dialog);


        // 커스텀 다이얼로그의 각 위젯들을 정의한다.
        final Button Button_info = dlg.findViewById(R.id.Button_info);
        final Switch Switch_BGM = dlg.findViewById(R.id.Switch_BGM);
        final Button Button_ok = dlg.findViewById(R.id.Button_ok);

        //쉐어드에서 설정값 가져와서 스위치에 세팅
        boolean bgm_on = preferences.getBoolean(KEY_BGM,true);
        Switch_BGM.setChecked(bgm_on);

        // 커스텀 다이얼로그를 노출한다.
        dlg.show();

        //게임 정보 클릭
        Button_info.setOnClickListener(v -> {
            Toast.makeText(context,"시민이라면 마피아를 처치하세요.\n마피아라면 시민들을 처치하세요.",Toast.LENGTH_SHORT).show();
            //게임정보 출력 추가예정
        });

        //배경음 스위치 변화
        Switch_BGM.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(KEY_BGM,isChecked);
                editor.commit();
            }
        });

        //확인 클릭 - 나가기
        Button_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dlg.dismiss();
            }
        });
    }
}