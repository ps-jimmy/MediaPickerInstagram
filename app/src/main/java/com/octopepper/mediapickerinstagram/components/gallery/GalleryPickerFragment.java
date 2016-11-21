package com.octopepper.mediapickerinstagram.components.gallery;

import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.octopepper.mediapickerinstagram.R;
import com.octopepper.mediapickerinstagram.commons.modules.LoadMoreModule;
import com.octopepper.mediapickerinstagram.commons.modules.LoadMoreModuleDelegate;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/*
 * Created by Guillaume on 17/11/2016.
 */

public class GalleryPickerFragment extends Fragment implements GridAdapterListener, LoadMoreModuleDelegate {

    @BindView(R.id.mGalleryRecyclerView)
    RecyclerView mGalleryRecyclerView;
    @BindView(R.id.mPreview)
    ImageView mPreview;
    @BindView(R.id.mAppBarContainer)
    AppBarLayout mAppBarContainer;

    private static final String EXTENSION_JPG = ".jpg";
    private static final String EXTENSION_JPEG = ".jpeg";
    private static final String EXTENSION_PNG = ".png";
    private static final int PREVIEW_SIZE = 800;
    private static final int MARGING_GRID = 2;
    private static final int RANGE = 10;

    private GridAdapter mGridAdapter;
    private LoadMoreModule mLoadMoreModule = new LoadMoreModule();
    private ArrayList<File> mFiles;
    private boolean isFirstLoad = true;
    private boolean isLoading = false;
    private int mOffset = 0;

    public static GalleryPickerFragment newInstance() {
        return new GalleryPickerFragment();
    }

    private void initViews() {
        if (isFirstLoad) {
            mGridAdapter = new GridAdapter(getContext());
        }
        mGridAdapter.setListener(this);
//        mLoadMoreModule.LoadMoreUtils(mGalleryRecyclerView, this, getContext());
        mGalleryRecyclerView.setAdapter(mGridAdapter);
        mGalleryRecyclerView.setHasFixedSize(true);
        mGalleryRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mGalleryRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view,
                                       RecyclerView parent, RecyclerView.State state) {
                outRect.left = MARGING_GRID;
                outRect.right = MARGING_GRID;
                outRect.bottom = MARGING_GRID;
                if (parent.getChildLayoutPosition(view) >= 0 && parent.getChildLayoutPosition(view) <= 3) {
                    outRect.top = MARGING_GRID;
                }
            }
        });

        fetchMedia();
    }

    private void fetchMedia() {
        mFiles = new ArrayList<>();
        File dirDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        parseDir(dirDownloads);
        File dirDcim = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        parseDir(dirDcim);
        File dirPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        parseDir(dirPictures);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            File dirDocuments = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
            parseDir(dirDocuments);
        }

        if (mFiles.size() > 0) {
            Picasso.with(getContext())
                    .load(Uri.fromFile(mFiles.get(0)))
                    .placeholder(R.drawable.placeholder_media)
                    .resize(PREVIEW_SIZE, PREVIEW_SIZE)
                    .centerCrop()
                    .into(mPreview);

            mGridAdapter.setItems(mFiles);
        }
        isFirstLoad = false;
    }

    private List<File> getRangePets() {
        int range = RANGE;
        if (mOffset < mFiles.size()) {
            if ((mOffset + range) < mFiles.size()) {
                return mFiles.subList(mOffset, mOffset + range);
            } else if ((mOffset + range) >= mFiles.size()) {
                return mFiles.subList(mOffset, mFiles.size());
            } else {
                return new ArrayList<>();
            }
        } else {
            return new ArrayList<>();
        }
    }

    private void parseDir(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            parseFileList(files);
        }
    }

    private void parseFileList(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {
                if (!file.getName().toLowerCase().startsWith(".")) {
                    parseDir(file);
                }
            } else {
                if (file.getName().toLowerCase().endsWith(EXTENSION_JPG)
                        || file.getName().toLowerCase().endsWith(EXTENSION_JPEG)
                        || file.getName().toLowerCase().endsWith(EXTENSION_PNG)) {
                    mFiles.add(file);
                }
            }
        }
    }

    private void loadNext() {
        mOffset += 10;
        List<File> files = getRangePets();
        if (files.size() > 0) {
            mGridAdapter.addItems(files, mGridAdapter.getItemCount());
        }
        isLoading = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.gallery_picker_view, container, false);
        ButterKnife.bind(this, v);
        initViews();
        return v;
    }

    @Override
    public void onClickMediaItem(File file) {
        Picasso.with(getContext())
                .load(Uri.fromFile(file))
                .noPlaceholder()
                .resize(PREVIEW_SIZE, PREVIEW_SIZE)
                .centerCrop()
                .noFade()
                .into(mPreview);

        mAppBarContainer.setExpanded(true, true);
    }

    @Override
    public void shouldLoadMore() {
        if (!isLoading) {
            isLoading = true;
            loadNext();
        }
    }
}
