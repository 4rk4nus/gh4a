package com.gh4a.loader;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.egit.github.core.Content;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.ContentService;
import org.eclipse.egit.github.core.util.EncodingUtils;

import android.content.Context;

import com.gh4a.Constants.LoaderResult;
import com.gh4a.Gh4Application;
import com.gh4a.utils.StringUtils;

public class GitModuleParserLoader extends BaseLoader {

    private String mRepoOwner;
    private String mRepoName;
    private String mPath;
    private String mRef;
    
    public GitModuleParserLoader(Context context, String repoOwner, String repoName, String path, String ref) {
        super(context);
        mRepoOwner = repoOwner;
        mRepoName = repoName;
        mPath = path;
        mRef = ref;
    }
    
    @Override
    public void doLoadInBackground(HashMap<Integer, Object> result) throws IOException {
        Gh4Application app = (Gh4Application) getContext().getApplicationContext();
        GitHubClient client = new GitHubClient();
        client.setOAuth2Token(app.getAuthToken());
        ContentService contentService = new ContentService(client);
        Content content = contentService.getContent(new RepositoryId(mRepoOwner, mRepoName), mPath, mRef);
        if (content != null) {
            String data = new String(EncodingUtils.fromBase64(content.getContent()));
            if (!StringUtils.isBlank(data)) {
                Map<String, String> gitModuleMap = new HashMap<String, String>();
                String[] lines = data.split("\n");
                String path = null;
                for (String line : lines) {
                    
                    line = line.trim();
                    if (line.startsWith("path = ")) {
                        String[] pathPart = line.split("=");
                        path = pathPart[1].trim();
                    }
                    
                    if (line.startsWith("url = ")) {
                        String[] urlPart = line.split("=");
                        String url = urlPart[1].trim();
                        String[] userRepoPart = url.split("/");
                        String user = userRepoPart[3];
                        String repo = userRepoPart[4];
                        
                        if (repo.lastIndexOf(".") != -1) {
                            repo = repo.substring(0, repo.lastIndexOf("."));
                        }
                        gitModuleMap.put(path, user + "/" + repo);
                    }
                }
                result.put(LoaderResult.DATA, gitModuleMap);
            }
        }
    }
}
