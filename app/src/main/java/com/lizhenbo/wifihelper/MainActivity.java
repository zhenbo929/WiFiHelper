package com.lizhenbo.wifihelper;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lizhenbo.wifihelper.adapter.WifiInfoAdapter;
import com.lizhenbo.wifihelper.model.WifiInfo;
import com.lizhenbo.wifihelper.utils.CodeUtil;
import com.lizhenbo.wifihelper.utils.ShellUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {

    private ListView mWifiInfoListView = null;//展示WiFiInfo的ListView
    private LinearLayout mErrorLayout = null;//发生错误时,展示的布局(1.解析文件错误2.没有root)
    private TextView mErrorMsg= null;//发生错误时,文字提示
    private List<WifiInfo> mWifiInfoList = new ArrayList<>();//保存WiFiInfo的List
    private WifiInfoAdapter mWifiInfoAdapter;//WiFiInfo的适配器
    private ClipboardManager mClipboardManager = null;//复制到剪切板的Manager
    ProgressDialog progressDialog;//加载时的进度条

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        readWifiConfigToListView();

    }

    /**
     * 初始化布局
     */
    private void initViews() {
        //弹出加载广告的进度条
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        progressDialog.show();
        mClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        mErrorMsg = (TextView) findViewById(R.id.tv_error_msg);
        mErrorLayout = (LinearLayout) findViewById(R.id.layout_error);
        mWifiInfoListView = (ListView) findViewById(R.id.listview_wifi_info);
        mWifiInfoAdapter = new WifiInfoAdapter(this, mWifiInfoList);
        mWifiInfoListView.setAdapter(mWifiInfoAdapter);
        mWifiInfoListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mClipboardManager.setPrimaryClip(ClipData.newPlainText(null,
                        MessageFormat.format("WIFI：{0} 密码：{1}", mWifiInfoList.get(position).getWifiName(), mWifiInfoList.get(position).getWifiPwd())));
                Toast.makeText(MainActivity.this, getString(R.string.copy_pwd_succeed_msg), Toast.LENGTH_LONG).show();
                return false;
            }
        });
    }

    /**
     * 读取WiFi配置文件
     */
    private void readWifiConfigToListView() {
        String[] commands = new String[]{"cat /data/misc/wifi/wpa_supplicant.conf\n", "exit\n"};

        ShellUtils.CommandResult cr = ShellUtils.execCommand(commands, true, true);

        if (cr.result != 0) {
            progressDialog.dismiss();
            mErrorLayout.setVisibility(View.VISIBLE);
            mErrorMsg.setText(getString(R.string.analysis_error_msg));
            Toast.makeText(this, getString(R.string.analysis_error_msg), Toast.LENGTH_LONG).show();
            return;
        }

        String wifiConfigInfo = String.valueOf(cr.successMsg);
        if (TextUtils.isEmpty(wifiConfigInfo)) {
            progressDialog.dismiss();
            mErrorLayout.setVisibility(View.VISIBLE);
            mErrorMsg.setText(getString(R.string.no_root_msg));
            Toast.makeText(MainActivity.this, getString(R.string.no_root_msg), Toast.LENGTH_LONG).show();
            return;
        }

        // 分析WiFi的配置文件，来获取WiFi信息
        Pattern network = Pattern.compile("network=\\{([^\\}]+)\\}", Pattern.DOTALL);
        Matcher matcher = network.matcher(wifiConfigInfo);
        WifiInfo model;
        //遍历所有节点，找到每个的WiFi信息（WiFi名字和密码）
        while (matcher.find()) {
            String networkBlock = matcher.group();
            Pattern ssid = Pattern.compile("ssid=([^\\s]+)\\s");
            Matcher ssidMatcher = ssid.matcher(networkBlock);
            // TODO: 2017/3/4 待测试功能 
            //这里来获取WiFi的名字，比较坑的是name的获取有两种形式
            //第一种：没有中文,ssid="name"
            //第二种：包含中文,ssid=2f2e25ac(中文utf-8格式的16进制数)
            //解决方式:先截取ssid=和\s空格中间的部分
            if (ssidMatcher.find()) {
                String WiFiname = ssidMatcher.group(1);
                if(WiFiname.contains("\"")){
                    WiFiname=WiFiname.replace("\"","");
                }else {
                    WiFiname= CodeUtil.convertUTF8ToString(WiFiname);
                }
                model = new WifiInfo();
                model.setWifiName(WiFiname);
                Pattern psk = Pattern.compile("psk=\"([^\"]+)\"");
                Matcher pskMatcher = psk.matcher(networkBlock);
                model.setWifiPwd(pskMatcher.find() ? pskMatcher.group(1) : getString(R.string.wifi_no_pwd));
                mWifiInfoList.add(model);

            }
        }
        Collections.reverse(mWifiInfoList);//把集合里的元素顺序颠倒一下，让最近连接的WiFi信息的在最上面
        mWifiInfoAdapter.notifyDataSetChanged();
        progressDialog.dismiss();
        Toast.makeText(MainActivity.this, getString(R.string.welcome_msg), Toast.LENGTH_SHORT).show();
    }

}
