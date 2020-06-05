package cn.pzw.tracklog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class RouteStepsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private ArrayList<String> steps = new ArrayList<>();

    private void initRecyclerView(){
        mRecyclerView = findViewById(R.id.recycler_view_route_steps_activity);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setAdapter(new RouteStepsListAdapter());
        mRecyclerView.setLayoutManager(linearLayoutManager);
    }

    private void initData(){
        Bundle bundle = getIntent().getBundleExtra("DATA");
        steps = bundle.getStringArrayList("step");
    }

    private void initToolbar(){
        mToolbar = findViewById(R.id.toolbar_route_steps_activity);
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route_steps);

        initToolbar();

        initData();

        initRecyclerView();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    class RouteStepsListAdapter extends RecyclerView.Adapter<RouteStepsListAdapter.MyHolder>{

        @NonNull
        @Override
        public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route_steps_activity_recycler_view, parent, false);
            return new MyHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyHolder holder, int position) {
            holder.textView.setText(steps.get(position));
        }

        @Override
        public int getItemCount() {
            return steps.size();
        }

        class MyHolder extends RecyclerView.ViewHolder{

            TextView textView;

            public MyHolder(@NonNull View itemView) {
                super(itemView);
                textView = (TextView)itemView.findViewById(R.id.tv_route_steps_activity_recycler_view_item);
            }
        }
    }
}