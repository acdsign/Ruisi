package xyz.yluo.ruisiapp.fragment;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import xyz.yluo.ruisiapp.R;
import xyz.yluo.ruisiapp.adapter.BaseAdapter;
import xyz.yluo.ruisiapp.adapter.HistoryAdapter;
import xyz.yluo.ruisiapp.database.MyDB;
import xyz.yluo.ruisiapp.model.ReadHistoryData;
import xyz.yluo.ruisiapp.widget.MyListDivider;

/**
 * Created by free2 on 16-7-14.
 * 收藏/主题/历史纪录
 * //todo 删除浏览历史
 */
public class FrageHistory extends BaseFragment {

    private List<ReadHistoryData> datas = new ArrayList<>();
    ;
    private HistoryAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        initToolbar(true, "浏览历史");
        addToolbarMenu(R.drawable.ic_delete_24dp).setOnClickListener(view -> {
            Dialog alertDialog = new AlertDialog.Builder(getActivity()).
                    setTitle("清空历史记录")
                    .setMessage("你确定要清空浏览历史吗？？")
                    .setPositiveButton("是的(=・ω・=)", (dialogInterface, i) -> {
                        MyDB db = new MyDB(getActivity());
                        db.clearHistory();
                        datas.clear();
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getActivity(), "浏览历史已清空~~", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .setCancelable(true)
                    .create();
            alertDialog.show();
        });
        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        SwipeRefreshLayout refreshLayout = (SwipeRefreshLayout) mRootView.findViewById(R.id.refresh_layout);
        refreshLayout.setEnabled(false);
        adapter = new HistoryAdapter(getActivity(), datas);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.addItemDecoration(new MyListDivider(getActivity(), MyListDivider.VERTICAL));
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        refresh();
        return mRootView;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.list_toolbar;
    }


    private void refresh() {
        datas.clear();
        adapter.notifyDataSetChanged();
        getDbData();
    }


    private void getDbData() {
        new GetUserHistoryTask().execute(1);
    }

    //获得历史记录
    private class GetUserHistoryTask extends AsyncTask<Integer, Void, List<ReadHistoryData>> {

        @Override
        protected List<ReadHistoryData> doInBackground(Integer... ints) {
            MyDB myDB = new MyDB(getActivity());
            return myDB.getHistory(50);
        }

        @Override
        protected void onPostExecute(List<ReadHistoryData> data) {
            super.onPostExecute(data);
            adapter.changeLoadMoreState(BaseAdapter.STATE_LOAD_NOTHING);
            if (data.size() > 0) {
                datas.addAll(data);
                adapter.notifyDataSetChanged();
            }
        }
    }
}
