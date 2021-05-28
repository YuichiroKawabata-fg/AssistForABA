package com.kawabata.abaprojects.assistforaba;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.kawabata.abaprojects.assistforaba.databinding.FragmentFirstBinding;

import java.util.Calendar;

import static android.content.Context.ALARM_SERVICE;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;
    private AlarmManager am;
    private PendingIntent pending;
    private int requestCode = 1;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(System.currentTimeMillis());
                // 5秒後にアラームが設定されるように決め打ち
                calendar.add(Calendar.SECOND, 5);
                //ブロードキャストレシーバーのIntentコールをおこなう設定（アラーム通知クラスをコールする）
                Intent intent = new Intent(getActivity().getApplicationContext(), AlarmNotification.class);
                intent.putExtra("RequestCode",requestCode);

                pending = PendingIntent.getBroadcast(
                        getActivity().getApplicationContext(),requestCode, intent, 0);

                // アラームをセットする
                am = (AlarmManager) getActivity().getSystemService(ALARM_SERVICE);

                if (am != null) {
                    am.setExact(AlarmManager.RTC_WAKEUP,
                            calendar.getTimeInMillis(), pending);

                    // トーストで設定されたことをを表示
                    Toast.makeText(getActivity().getApplicationContext(),
                            "alarm start", Toast.LENGTH_SHORT).show();

                    Log.d("debug", "start");
                }

                //Fragment2に遷移する（サンプル）
//                NavHostFragment.findNavController(FirstFragment.this)
//                        .navigate(R.id.action_FirstFragment_to_SecondFragment);
            }
        });


        //キャンセルを行うボタン
        //同じリクエストコードを設定することで既に設定してあるアラームを解除することができる
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent indent = new Intent(getActivity().getApplicationContext(), AlarmNotification.class);
                PendingIntent pending = PendingIntent.getBroadcast(
                        getActivity().getApplicationContext(), requestCode, indent, 0);

                // アラームを解除する
                AlarmManager am = (AlarmManager)getActivity().
                        getSystemService(ALARM_SERVICE);
                if (am != null) {
                    am.cancel(pending);
                    Toast.makeText(getActivity().getApplicationContext(),
                            "alarm cancel", Toast.LENGTH_SHORT).show();
                    Log.d("debug", "cancel");
                }
                else{
                    Log.d("debug", "null");
                }

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}