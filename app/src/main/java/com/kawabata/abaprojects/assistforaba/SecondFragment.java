package com.kawabata.abaprojects.assistforaba;

import android.app.LauncherActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kawabata.abaprojects.assistforaba.databinding.FragmentSecondBinding;
import com.kawabata.abaprojects.assistforaba.listcomponent.ListItem;
import com.kawabata.abaprojects.assistforaba.utill.AlarmListAdapter;
import com.kawabata.abaprojects.assistforaba.utill.DatabaseHelper;

import java.util.ArrayList;
import java.util.Locale;

public class SecondFragment extends Fragment {

    private FragmentSecondBinding binding;
    private DatabaseHelper helper = null;
    final static public int NEW_REQ_CODE = 1;
    final static public int EDIT_REQ_CODE = 2;
    private RecyclerView recyclerView;

    private final String[] dataset = new String[20];

    private static final int RESULT_PICK_IMAGEFILE = 1001;
    private TextView textView;
    private ImageView imageView;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        binding = FragmentSecondBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d("Life Cycle","secondFragment onViewCreated");
        recyclerView = (RecyclerView)view.findViewById(R.id.my_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        recyclerView.setHasFixedSize(true);
        // use a linear layout manager
        RecyclerView.LayoutManager rLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(rLayoutManager);

//新規アクション作成ボタン
        binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), InputAlarmActivity.class);
                intent.putExtra("ACTION",0);
                startActivity(intent);
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Life Cycle", "Fragment-onResume");
        //DBから登録されたアラームの一覧を取得してリサイクルビューのアダプタにセット
        //アラーム登録画面に遷移したあと、アラーム登録語に再描画されるよう、この場所に記載
        AlarmListAdapter adapter = new AlarmListAdapter(getActivity(), loadAlarms()){
            @Override
            protected void onClickedListItem(@NonNull ListItem listItem) {
                super.onClickedListItem(listItem);
                //Alarm Listをタップしたときの挙動
                Intent intent = new Intent(getActivity(), InputAlarmActivity.class);
                intent.putExtra(getString(R.string.request_code),EDIT_REQ_CODE);
                intent.putExtra(getString(R.string.alarm_id),listItem.getAlarmID());
                Log.d("ALARMID",String.valueOf(listItem.getAlarmID()));
                startActivity(intent);
              }
        };
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    private ArrayList<ListItem> loadAlarms(){
        helper = DatabaseHelper.getInstance(getActivity());

        ArrayList<ListItem> data = new ArrayList<>();

        try(SQLiteDatabase db = helper.getReadableDatabase()) {

            String[] cols ={"alarmid","name","alarttime","uri"};

            Cursor cs = db.query("alarms",cols,null,null,
                    null,null,"alarmid",null);
            boolean eol = cs.moveToFirst();
            while (eol){
                ListItem item = new ListItem();
                item.setAlarmID(cs.getInt(0));
                item.setAlarmName(cs.getString(1));
                item.setTime(cs.getString(2));
                item.setUri(cs.getString(3));
                data.add(item);
                eol = cs.moveToNext();
            }
        }
        return data;
    }

}