package com.guihgo.inventorycontroll.ui.tags;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import com.guihgo.inventorycontroll.Helper;
import com.guihgo.inventorycontroll.R;
import com.guihgo.inventorycontroll.database.DatabaseHelper;
import com.guihgo.inventorycontroll.database.TagContract;
import com.guihgo.inventorycontroll.model.tag.TagEntity;
import com.guihgo.inventorycontroll.databinding.FragmentTagsBinding;

import java.util.ArrayList;

public class TagsFragment extends Fragment {

    private FragmentTagsBinding binding;

    private RecyclerView recyclerView;
    protected TagListAdapter tagListAdapter;

    private BottomNavigationView bnvMenu;

    DatabaseHelper dbHelper;
    SQLiteDatabase db;

    ActivityResultLauncher<Intent> startForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if(result.getResultCode() == Activity.RESULT_OK) {
                refreshData();
            }
        }
    });

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        TagsViewModel tagsViewModel =
                new ViewModelProvider(this).get(TagsViewModel.class);

        binding = FragmentTagsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        recyclerView = binding.recyclerView;
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        tagListAdapter = new TagListAdapter(null);
        tagListAdapter.setupRecycleView(recyclerView);

        dbHelper = new DatabaseHelper(getContext());
        db = dbHelper.getWritableDatabase();

        this.refreshData();

        tagListAdapter.setOnItemInteractionListener(new TagListAdapter.OnItemInteractionListener() {
            @Override
            public void onItemSwipedLeft(int position) {

                TagEntity item = tagListAdapter.tagEntityList.get(position);

                MaterialAlertDialogBuilder madbConfirm = new MaterialAlertDialogBuilder(getContext());

                String title = getContext().getString(R.string.dialog_confirm_remove_title);

                String message = getContext().getString(R.string.dialog_confirm_remove_message)
                        .replace("{{entity_name}}", TagContract.TagEntry.TABLE_NAME)
                        .replace("{{entity_id}}", item.name + "(#"+item.id+")");

                madbConfirm.setTitle(title);
                madbConfirm.setMessage(message);

                madbConfirm.setNeutralButton(getResources().getString(R.string.dialog_confirm_remove_button_cancel), (dialog, which) -> {
                    tagListAdapter.notifyItemChanged(position);
                });
                madbConfirm.setPositiveButton(getResources().getString(R.string.dialog_confirm_remove_button_remove), (dialog, which) -> {

                    String where = TagContract.TagEntry.COLUMN_NAME_ID + " = ?";
                    String[] whereArgs = {item.id};
                    db.delete(TagContract.TagEntry.TABLE_NAME, where, whereArgs);
                    tagListAdapter.tagEntityList.remove(position);
                    tagListAdapter.notifyItemRemoved(position);
                });

                madbConfirm.show();
            }

            @Override
            public void onItemSwipedRight(int position) {

            }

            @Override
            public void onItemClick(int position) {
                Intent intent = new Intent(getActivity(), TagAddEdit.class);
                intent.putExtra(TagAddEdit.KEY_ID, tagListAdapter.tagEntityList.get(position).id);
                startForResult.launch(intent);
            }
        });

        bnvMenu = binding.bottomNavigation;

        bnvMenu.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                Helper.confirmTouch(getContext());
                switch (item.getItemId()) {
                    case R.id.tags_add:
                        startForResult.launch(new Intent(getActivity(), TagAddEdit.class));
                        return true;
                }

                return false;
            }
        });

        return root;
    }

    void refreshData() {
        tagListAdapter.tagEntityList = new ArrayList<>();
        String[] projection = {
                TagContract.TagEntry.COLUMN_NAME_ID,
                TagContract.TagEntry.COLUMN_NAME_NAME,
                TagContract.TagEntry.COLUMN_NAME_DESCRIPTION
        };

        String sortOrder = TagContract.TagEntry.COLUMN_NAME_NAME + " ASC";

        Cursor cursor = db.query(TagContract.TagEntry.TABLE_NAME, projection, null,null,null,null, sortOrder);

        while(cursor.moveToNext()) {
            tagListAdapter.tagEntityList.add(new TagEntity(
                    cursor.getString(cursor.getColumnIndexOrThrow(TagContract.TagEntry.COLUMN_NAME_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TagContract.TagEntry.COLUMN_NAME_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(TagContract.TagEntry.COLUMN_NAME_DESCRIPTION))
            ));
        }
        cursor.close();

        tagListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
    }

    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        return super.onGetLayoutInflater(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}