package com.gh4a.loader;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.RepositoryService;

import android.content.Context;

import com.gh4a.Constants.LoaderResult;
import com.gh4a.Gh4Application;

public class BranchListLoader extends BaseLoader {

    private String mRepoOwner;
    private String mRepoName;
    
    public BranchListLoader(Context context, String repoOwner, String repoName) {
        super(context);
        mRepoOwner = repoOwner;
        mRepoName = repoName;
    }
    
    @Override
    public void doLoadInBackground(HashMap<Integer, Object> result) throws IOException {
        Gh4Application app = (Gh4Application) getContext().getApplicationContext();
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(app.getAuthToken());
        RepositoryService repoService = new RepositoryService(client);
        result.put(LoaderResult.DATA, repoService.getBranches(new RepositoryId(mRepoOwner, mRepoName)));
    }
}
