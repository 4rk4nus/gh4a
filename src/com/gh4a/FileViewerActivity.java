/*
 * Copyright 2011 Azwan Adli Abdullah
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gh4a;

import java.util.HashMap;

import org.eclipse.egit.github.core.Content;
import org.eclipse.egit.github.core.util.EncodingUtils;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.gh4a.Constants.LoaderResult;
import com.gh4a.loader.ContentLoader;
import com.gh4a.utils.FileUtils;
import com.gh4a.utils.StringUtils;

public class FileViewerActivity extends BaseSherlockFragmentActivity 
    implements LoaderManager.LoaderCallbacks<Object> {

    protected String mRepoOwner;
    protected String mRepoName;
    private String mPath;
    private String mRef;
    private String mSha;
    private String mName;
    private Content mContent;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setTheme(Gh4Application.THEME);
        super.onCreate(savedInstanceState);

        mRepoOwner = getIntent().getStringExtra(Constants.Repository.REPO_OWNER);
        mRepoName = getIntent().getStringExtra(Constants.Repository.REPO_NAME);
        mPath = getIntent().getStringExtra(Constants.Object.PATH);
        mRef = getIntent().getStringExtra(Constants.Object.REF);
        mSha = getIntent().getStringExtra(Constants.Object.OBJECT_SHA);
        mName = getIntent().getStringExtra(Constants.Object.NAME);
        
        if (!isOnline()) {
            setErrorView();
            return;
        }
        
        setContentView(R.layout.web_viewer);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(mName);
        actionBar.setSubtitle(mRepoOwner + "/" + mRepoName);
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        showLoading();
        getSupportLoaderManager().initLoader(0, null, this);
        getSupportLoaderManager().getLoader(0).forceLoad();
    }

    private void fillData(boolean highlight) {
        String data = new String(EncodingUtils.fromBase64(mContent.getContent()));
        WebView webView = (WebView) findViewById(R.id.web_view);

        WebSettings s = webView.getSettings();
        s.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        s.setAllowFileAccess(true);
        s.setBuiltInZoomControls(true);
        s.setLightTouchEnabled(true);
        s.setLoadsImagesAutomatically(true);
        s.setPluginsEnabled(false);
        s.setSupportZoom(true);
        s.setSupportMultipleWindows(true);
        s.setJavaScriptEnabled(true);
        s.setUseWideViewPort(true);

        webView.setWebViewClient(webViewClient);
        if (FileUtils.isImage(mName)) {
            String htmlImage = StringUtils.highlightImage("https://github.com/" + mRepoOwner + "/" + mRepoName + "/raw/" + mRef + "/" + mPath);
            webView.loadDataWithBaseURL("file:///android_asset/", htmlImage, "text/html", "utf-8", "");
        }
        else {
            String highlighted = StringUtils.highlightSyntax(data, highlight, mName);
            webView.loadDataWithBaseURL("file:///android_asset/", highlighted, "text/html", "utf-8", "");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.download_menu, menu);
        
        if (Gh4Application.THEME != R.style.LightTheme) {
            menu.getItem(0).setIcon(R.drawable.download_dark);
            menu.getItem(1).setIcon(R.drawable.web_site_dark);
        }
        
        menu.removeItem(R.id.download);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean setMenuOptionItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getApplicationContext().openRepositoryInfoActivity(this, mRepoOwner, mRepoName, Intent.FLAG_ACTIVITY_CLEAR_TOP);
                return true;     
            case R.id.browser:
                String blobUrl = "https://github.com/" + mRepoOwner + "/" + mRepoName + "/blob/" + mRef + "/" + mPath + "?raw=true";
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(blobUrl));
                startActivity(browserIntent);
                return true;

            default:
                return true;
        }
    }
    
    private WebViewClient webViewClient = new WebViewClient() {

        @Override
        public void onPageFinished(WebView webView, String url) {
            hideLoading();
        }
        
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    };

    @Override
    public Loader onCreateLoader(int arg0, Bundle arg1) {
        return new ContentLoader(this, mRepoOwner, mRepoName, mPath, mRef);
    }

    @Override
    public void onLoadFinished(Loader loader, Object object) {
        HashMap<Integer, Object> result = (HashMap<Integer, Object>) object;
        hideLoading();
        
        if (!isLoaderError(result)) {
            if (object != null) {
                mContent = (Content) result.get(LoaderResult.DATA);
                fillData(true);
            }    
        }
    }

    @Override
    public void onLoaderReset(Loader arg0) {
        // TODO Auto-generated method stub
        
    }

}
