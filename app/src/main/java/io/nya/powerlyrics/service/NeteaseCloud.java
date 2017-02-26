package io.nya.powerlyrics.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import io.nya.powerlyrics.model.LyricResult;
import io.nya.powerlyrics.model.SearchResult;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Download from netease cloud music
 */

public class NeteaseCloud {

    private OkHttpClient client = new OkHttpClient();

    private static final MediaType FORM_TYPE = MediaType.parse("application/x-www-form-urlencoded");

    public static class Result<T> {
        public T result;
        public int code;
    }

    private Request.Builder buildRequest(String url) {
        return new Request.Builder()
                .url(url)
                .header("Accept", "*/*")
//                .addHeader("Accept-Encoding", "gzip,deflate,sdch")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.8,gl;q=0.6,zh-TW;q=0.4")
                .addHeader("Connection", "Keep-alive")
//                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .addHeader("Host", "music.163.com")
                .addHeader("Referer", "http://music.163.com/search/")
                .addHeader("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36");
    }

    public SearchResult searchMusic(String name) throws IOException {
        return searchMusic(name, 0, 20);
    }

    public SearchResult searchMusic(String name, int offset, int limit) throws IOException {
        RequestBody body = RequestBody.create(FORM_TYPE, "s=" + name + "&type=1&offset=" + offset + "&total=true&limit=" + limit);
        String url = "http://music.163.com/api/search/get";
        Request request = buildRequest(url)
                .post(body)
                .build();

        Response response = client.newCall(request).execute();
        String resultStr = response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        TypeReference ref = new TypeReference<Result<SearchResult>>() {};
        Result<SearchResult> result = mapper.readValue(resultStr, ref);
        return result.result;
    }

    public LyricResult getLyric(int musicId) throws IOException {
        String url = "http://music.163.com/api/song/lyric?os=osx&id=" + musicId + "&lv=-1&kv=-1&tv=-1";
        Request request = buildRequest(url)
                .build();

        Response response = client.newCall(request).execute();
        String resultStr = response.body().string();
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(resultStr, LyricResult.class);
    }
}
