package com.dostchat.dost.fragments.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dostchat.dost.R;
import com.dostchat.dost.adapters.recyclerView.contacts.ContactsAdapter;
import com.dostchat.dost.app.AppConstants;
import com.dostchat.dost.app.DostChatApp;
import com.dostchat.dost.helpers.AppHelper;
import com.dostchat.dost.helpers.PreferenceManager;
import com.dostchat.dost.interfaces.LoadingData;
import com.dostchat.dost.models.messages.ConversationsModel;
import com.dostchat.dost.models.users.Pusher;
import com.dostchat.dost.models.users.contacts.ContactsModel;
import com.dostchat.dost.models.users.contacts.PusherContacts;
import com.dostchat.dost.presenters.users.ContactsPresenter;
import com.dostchat.dost.ui.RecyclerViewFastScroller;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsFragment extends Fragment implements LoadingData {

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;

    private List<ContactsModel> mContactsModelList;
    private ContactsAdapter mContactsAdapter;
    private ContactsPresenter mContactsPresenter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        mContactsPresenter = new ContactsPresenter(this);
        mContactsPresenter.onCreate();
        initializerView();
        return mView;
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mContactsAdapter = new ContactsAdapter(getActivity(), mContactsModelList);
        setHasOptionsMenu(true);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mContactsAdapter);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.getItemAnimator().setChangeDuration(0);
        ContactsList.setHasFixedSize(true);
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_contacts:
                mContactsPresenter.onRefresh();
                break;
        }
        return true;
    }


    /**
     * method to show contacts list
     *
     * @param contactsModels this is parameter for  ShowContacts method
     */
    public void ShowContacts(List<ContactsModel> contactsModels, boolean isRefresh) {
        if (!isRefresh) {
            mContactsModelList = contactsModels;
        } else {
            mContactsAdapter.setContacts(contactsModels);
        }
        if (contactsModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);

        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * method to update contacts
     *
     * @param contactsModels this is parameter for  updateContacts method
     */
    public void updateContacts(List<ContactsModel> contactsModels) {
        Realm realm = DostChatApp.getRealmDatabaseInstance();
        this.mContactsModelList = contactsModels;
        mContactsPresenter.getContacts(true);
        for (ContactsModel contactsModel : contactsModels) {
            if (contactsModel.isLinked() && contactsModel.isActivate()) {
                int ConversationID = getConversationId(contactsModel.getId(), PreferenceManager.getID(getActivity()), realm);
                if (ConversationID != 0) {
                    realm.executeTransaction(realm1 -> {
                        ConversationsModel conversationsModel = realm1.where(ConversationsModel.class).equalTo("id", ConversationID).findFirst();
                        conversationsModel.setRecipientImage(contactsModel.getImage());
                        conversationsModel.setRecipientPhone(contactsModel.getPhone());
                        realm1.copyToRealmOrUpdate(conversationsModel);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, ConversationID));
                    });
                }

            }

        }
        realm.close();
    }

    /**
     * method to get a conversation id
     *
     * @param recipientId this is the first parameter for getConversationId method
     * @param senderId    this is the second parameter for getConversationId method
     * @return conversation id
     */
    private int getConversationId(int recipientId, int senderId, Realm realm) {
        try {
            ConversationsModel conversationsModelNew = realm.where(ConversationsModel.class)
                    .beginGroup()
                    .equalTo("RecipientID", recipientId)
                    .or()
                    .equalTo("RecipientID", senderId)
                    .endGroup().findAll().first();
            return conversationsModelNew.getId();
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception ContactFragment" + e.getMessage());
            return 0;
        }
    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    public void onEventMainThread(PusherContacts pusher) {
        mContactsPresenter.onEventMainThread(pusher);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mContactsPresenter.onDestroy();
    }


    @Override
    public void onShowLoading() {
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_START_REFRESH));
    }

    @Override
    public void onHideLoading() {
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_STOP_REFRESH));
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_STOP_REFRESH));
    }
}