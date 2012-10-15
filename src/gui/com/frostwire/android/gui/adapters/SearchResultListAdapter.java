/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011, 2012, FrostWire(TM). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.frostwire.android.gui.adapters;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.frostwire.android.R;
import com.frostwire.android.core.ConfigurationManager;
import com.frostwire.android.core.Constants;
import com.frostwire.android.core.MediaType;
import com.frostwire.android.gui.activities.MainActivity;
import com.frostwire.android.gui.search.BittorrentSearchResult;
import com.frostwire.android.gui.search.SearchResult;
import com.frostwire.android.gui.search.WebEngineSearchResult;
import com.frostwire.android.gui.services.Engine;
import com.frostwire.android.gui.transfers.DownloadTransfer;
import com.frostwire.android.gui.transfers.InvalidTransfer;
import com.frostwire.android.gui.transfers.TransferManager;
import com.frostwire.android.gui.util.UIUtils;
import com.frostwire.android.gui.views.AbstractListAdapter;
import com.frostwire.android.gui.views.NewTransferDialog;
import com.frostwire.android.gui.views.NewTransferDialog.OnYesNoListener;
import com.frostwire.android.util.FilenameUtils;

/**
 * @author gubatron
 * @author aldenml
 *
 */
public class SearchResultListAdapter extends AbstractListAdapter<SearchResult> {

    private static final String TAG = "FW.SearchResultListAdapter";

    private final Map<String, Drawable> drawableCache;
    private final DownloadClickListener downloadClickListener;

    public SearchResultListAdapter(Context context, List<SearchResult> list) {
        super(context, R.layout.view_bittorrent_search_result_list_item, list);

        drawableCache = new HashMap<String, Drawable>();
        downloadClickListener = new DownloadClickListener();
    }

    public void filter(int mediaTypeId) {
        visualList = new ArrayList<SearchResult>();
        for (SearchResult sr : new ArrayList<SearchResult>(list)) {
            if (accept(sr, mediaTypeId)) {
                visualList.add(sr);
            }
        }
        notifyDataSetInvalidated();
    }

    public boolean accept(SearchResult sr, int mediaTypeId) {
        MediaType mt = MediaType.getMediaTypeForExtension(FilenameUtils.getExtension(sr.getFileName()));
        if (mt == null) {
            return false;
        }
        return mt.getId() == mediaTypeId;
    }

    @Override
    protected void populateView(View view, SearchResult sr) {
        if (sr instanceof BittorrentSearchResult) {
            populateBittorrentView(view, (BittorrentSearchResult) sr);
        } else {
            populateWebEngineView(view, sr);
        }
    }

    protected void populateBittorrentView(View view, BittorrentSearchResult sr) {
        ImageView fileTypeIcon = findView(view, R.id.view_bittorrent_search_result_list_item_filetype_icon);
        fileTypeIcon.setImageDrawable(getDrawable(FilenameUtils.getExtension(sr.getFileName())));

        TextView title = findView(view, R.id.view_bittorrent_search_result_list_item_title);
        title.setText(sr.getTitle());
        // if marked as downloading
        // title.setTextColor(GlobalConstants.COLOR_DARK_BLUE);

        TextView fileSize = findView(view, R.id.view_bittorrent_search_result_list_item_file_size);
        fileSize.setText(UIUtils.getBytesInHuman(sr.getSize()));

        TextView extra = findView(view, R.id.view_bittorrent_search_result_list_item_text_extra);
        extra.setText(FilenameUtils.getExtension(sr.getFileName()));

        TextView seeds = findView(view, R.id.view_bittorrent_search_result_list_item_text_seeds);
        seeds.setText(getContext().getResources().getQuantityString(R.plurals.count_seeds_source, sr.getRank(), sr.getRank()));

        TextView sourceLink = findView(view, R.id.view_bittorrent_search_result_list_item_text_source);
        sourceLink.setText(Html.fromHtml("<a href=\"" + sr.getDetailsUrl() + "\">" + sr.getSource() + "</a>"), TextView.BufferType.SPANNABLE);
        sourceLink.setOnClickListener(new OnLinkClickListener(sr.getDetailsUrl()));

        /**
        ImageButton downloadButton = findView(view, R.id.view_bittorrent_search_result_list_item_button_download);
        downloadButton.setTag(sr);
        downloadButton.setOnClickListener(downloadClickListener);
        */
    }

    protected void populateWebEngineView(View view, SearchResult sr) {
        ImageView fileTypeIcon = findView(view, R.id.view_bittorrent_search_result_list_item_filetype_icon);
        fileTypeIcon.setImageDrawable(getDrawable(FilenameUtils.getExtension(sr.getFileName())));

        TextView title = findView(view, R.id.view_bittorrent_search_result_list_item_title);
        title.setText(sr.getTitle());
        // if marked as downloading
        // title.setTextColor(GlobalConstants.COLOR_DARK_BLUE);

        TextView fileSize = findView(view, R.id.view_bittorrent_search_result_list_item_file_size);
        //fileSize.setText(UIUtils.getBytesInHuman(sr.getSize()));
        fileSize.setText("");

        TextView extra = findView(view, R.id.view_bittorrent_search_result_list_item_text_extra);
        extra.setText(FilenameUtils.getExtension(sr.getFileName()));

        TextView seeds = findView(view, R.id.view_bittorrent_search_result_list_item_text_seeds);
        //seeds.setText(getContext().getResources().getQuantityString(R.plurals.count_seeds_source, sr.getRank(), sr.getRank()));
        seeds.setText("");

        TextView sourceLink = findView(view, R.id.view_bittorrent_search_result_list_item_text_source);
        sourceLink.setText(Html.fromHtml("<a href=\"" + sr.getDetailsUrl() + "\">" + sr.getSource() + "</a>"), TextView.BufferType.SPANNABLE);
        sourceLink.setOnClickListener(new OnLinkClickListener(sr.getDetailsUrl()));
    }

    @Override
    protected void onItemClicked(View v) {
        downloadClickListener.onClick(v);
    }

    protected void onTransferStarted(DownloadTransfer transfer) {
    }

    private Drawable getDrawable(String ext) {
        Drawable d;
        if (drawableCache.containsKey(ext)) {
            d = drawableCache.get(ext);
        } else {
            d = getContext().getResources().getDrawable(getFileTypeIconId(ext));
            drawableCache.put(ext, d);
        }

        return d;
    }

    private static int getFileTypeIconId(String ext) {
        MediaType mt = MediaType.getMediaTypeForExtension(ext);
        if (mt == null) {
            return R.drawable.question_mark;
        }
        if (mt.equals(MediaType.getApplicationsMediaType())) {
            return R.drawable.browse_peer_application_icon_selector_off;
        } else if (mt.equals(MediaType.getAudioMediaType())) {
            return R.drawable.browse_peer_audio_icon_selector_off;
        } else if (mt.equals(MediaType.getDocumentMediaType())) {
            return R.drawable.browse_peer_document_icon_selector_off;
        } else if (mt.equals(MediaType.getImageMediaType())) {
            return R.drawable.browse_peer_picture_icon_selector_off;
        } else if (mt.equals(MediaType.getVideoMediaType())) {
            return R.drawable.browse_peer_video_icon_selector_off;
        } else if (mt.equals(MediaType.getTorrentMediaType())) {
            return R.drawable.browse_peer_torrent_icon_selector_off;
        } else {
            return R.drawable.question_mark;
        }
    }

    private void startTransfer(final SearchResult sr) {
        NewTransferDialog dlg = new NewTransferDialog(getContext(), sr, false, new OnYesNoListener() {
            public void onYes(NewTransferDialog dialog) {
                // putting this logic in a thread to avoid ANR errors. Needs refactor to avoid context leaks
                Engine.instance().getThreadPool().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DownloadTransfer transfer = TransferManager.instance().download(sr);
                            Looper.prepare();
                            if (!(transfer instanceof InvalidTransfer)) {
                                UIUtils.showShortMessage(getContext(), R.string.download_added_to_queue);

                                if (ConfigurationManager.instance().showTransfersOnDownloadStart()) {
                                    Intent i = new Intent(getContext(), MainActivity.class);
                                    i.setAction(Constants.ACTION_SHOW_TRANSFERS);
                                    i.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                                    getContext().startActivity(i);
                                }

                                onTransferStarted(transfer);
                            } else {
                                UIUtils.showShortMessage(getContext(), ((InvalidTransfer) transfer).getReasonResId());
                            }
                        } catch (Throwable e) {
                            Log.e(TAG, "Error adding new download from result: " + sr, e);
                        }
                    }
                });
            }

            public void onNo(NewTransferDialog dialog) {
            }
        });

        dlg.show(); // this dialog will leak if the context is destroyed. Find a solution.
    }

    private class OnLinkClickListener implements OnClickListener {

        private final String url;

        public OnLinkClickListener(String url) {
            this.url = url;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            getContext().startActivity(i);
        }
    }

    private final class DownloadClickListener implements OnClickListener {
        public void onClick(View v) {
            SearchResult sr = (SearchResult) v.getTag();
            startTransfer(sr);
        }
    }
}
