package xyz.yluo.ruisiapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;
import xyz.yluo.ruisiapp.R;
import xyz.yluo.ruisiapp.adapter.ChatListAdapter;
import xyz.yluo.ruisiapp.data.ChatListData;
import xyz.yluo.ruisiapp.utils.AsyncHttpCilentUtil;
import xyz.yluo.ruisiapp.utils.ConfigClass;
import xyz.yluo.ruisiapp.utils.PostHander;

/**
 * Created by free2 on 16-3-30.
 * 聊天activity
 * TODO 支持翻页。。。。目前只能看最后一页
 */
public class ChatActivity extends AppCompatActivity{

    @Bind(R.id.topic_recycler_view)
    protected RecyclerView recycler_view;
    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.load_View)
    protected LinearLayout load_View;
    @Bind(R.id.action_smiley)
    protected ImageButton action_smiley;
    @Bind(R.id.smiley_container)
    protected LinearLayout smiley_container;
    @Bind(R.id.input_aera)
    protected EditText input_aera;

    private List<ChatListData> datas = new ArrayList<>();
    private ChatListAdapter adapter;

    private String replyUrl = "";
    private String username = "消息";
    private String url = "";
    private String hash = ConfigClass.CONFIG_FORMHASH;
    private String touid = "";

    public static void open(Context context, String username,String url) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra("username", username);
        intent.putExtra("url",url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        try {
            Bundle bundle = this.getIntent().getExtras();
            username = bundle.getString("username");
            url = bundle.getString("url");
        }catch (Exception e){
            System.out.print(e);
        }

        setSupportActionBar(toolbar);
        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!=null){
            actionBar.setTitle(username);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        adapter = new ChatListAdapter(this,datas);

        recycler_view.setLayoutManager(layoutManager);
        recycler_view.setAdapter(adapter);

        action_smiley.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(smiley_container.getVisibility()==View.VISIBLE){
                    smiley_container.setVisibility(View.GONE);
                }else{
                    smiley_container.setVisibility(View.VISIBLE);
                }

            }
        });

        getData();

    }

    private void getData(){

        load_View.setVisibility(View.VISIBLE);
        AsyncHttpCilentUtil.get(getApplicationContext(), url, null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                new GetDataTask(new String(responseBody)).execute();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });
    }

    @OnClick(R.id.action_send)
    protected void action_send_click(){
        smiley_container.setVisibility(View.GONE);
        hide_ime();
        //按钮监听
        post_reply(input_aera.getText().toString());

    }

    @OnClick({R.id._1000, R.id._1001,R.id._1002,R.id._1003,R.id._1005,
            R.id._1006,R.id._1007,R.id._1008,R.id._1009,R.id._1010,
            R.id._1011,R.id._1012,R.id._1013,R.id._1014,R.id._1015,
            R.id._1016,R.id._1017,R.id._1018,R.id._1019,R.id._1020,
            R.id._1021,R.id._1022,R.id._1023,R.id._1024,R.id._1025,
            R.id._1027,R.id._1028,R.id._1029,R.id._1030, R.id._998,
            R.id._999,R.id._9998,R.id._9999
    })
    protected void smiley_click(ImageButton btn){
        //插入表情
        //{:16_1021:}
        //_1021
        //input_aera.append(btn.getTag().toString());
        String tmp = btn.getTag().toString();
        PostHander hander = new PostHander(getApplicationContext(),input_aera);
        hander.insertSmiley("{:16" + tmp + ":}", btn.getDrawable());
    }

    public class GetDataTask extends AsyncTask<Void,Void,String> {
        //* 传入一篇文章html
        //* 返回list<ChatListData>

        private String htmlData;
        public GetDataTask(String htmlData) {
            this.htmlData = htmlData;
        }

        @Override
        protected String doInBackground(Void... params) {
            //int type String userImage, String content, String time
            String content = "未能获取数据";
            String userimg = "";
            String posttime = "";
            int type = 0;

            //list 所有楼数据
            Document doc = Jsoup.parse(htmlData);
            //获取回复/hash
            if (doc.select("#pmform")!= null) {
                replyUrl = doc.select("form#pmform").attr("action");
                hash = doc.select("input[name=formhash]").attr("value"); // 具有 formhash 属性的链接
                touid = doc.select("input[name=touid]").attr("value");
            }
            Elements elements = doc.select(".msgbox.b_m");
            Elements listdata = elements.select(".cl");

            for(Element temp:listdata){
                //左边
                if(temp.attr("class").contains("friend_msg")){
                    type =0 ;
                }else{//右边
                    type = 1;
                }
                userimg = temp.select(".avat").select("img").attr("src");
                content = temp.select(".dialog_t").html();
                posttime = temp.select(".date").text();

                datas.add(new ChatListData(type,userimg,content,posttime));
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            super.onPostExecute(s);
            adapter.notifyDataSetChanged();
            load_View.setVisibility(View.GONE);

        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_article_normal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
            return true;
        }else if(id==R.id.menu_refresh){
            refresh();
        }
        return super.onOptionsItemSelected(item);
    }

    private void refresh(){
        datas.clear();
        adapter.notifyDataSetChanged();
        getData();
    }

    private void hide_ime(){
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void post_reply(String text){

        if(text==""){
            Toast.makeText(getApplicationContext(),"你还没有输入内容！！！",Toast.LENGTH_SHORT).show();
        }else {

            final ProgressDialog progress;
            progress = ProgressDialog.show(this, "正在发送",
                    "请等待", true);
            //尝试回复
            /*
            message:帮顶
            formhash:70af5bb6
            */
            RequestParams params = new RequestParams();
            params.put("formhash", hash);
            params.put("touid",touid);
            params.put("message", text);

            AsyncHttpCilentUtil.post(getApplicationContext(), replyUrl, params, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    String res = new String(responseBody);
                    if (res.contains("操作成功")) {
                        send_success();
                        hide_ime();
                        progress.dismiss();
                    } else {
                        progress.dismiss();
                        if(res.contains("两次发送短消息太快")){
                            Toast.makeText(getApplicationContext(),"两次发送短消息太快，请稍候再发送",Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(getApplicationContext(), "由于未知原因发表失败"+res, Toast.LENGTH_SHORT).show();
                        }

                    }
                }
                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                    Toast.makeText(getApplicationContext(), "网络错误！！！", Toast.LENGTH_SHORT).show();
                    progress.dismiss();
                }
            });
        }
    }

    private void send_success(){
        //http://rs.xidian.edu.cn/ucenter/avatar.php?uid=252553&size=small
        String userImage = ConfigClass.BBS_BASE_URL+"ucenter/avatar.php?uid="+ConfigClass.CONFIG_USER_UID+"&size=small";
        datas.add(new ChatListData(1,userImage,input_aera.getText().toString(),"刚刚"));
        input_aera.setText("");
        adapter.notifyItemInserted(datas.size()-1);
        smiley_container.setVisibility(View.GONE);
        Toast.makeText(getApplicationContext(),"发布成功",Toast.LENGTH_SHORT).show();
    }
}
