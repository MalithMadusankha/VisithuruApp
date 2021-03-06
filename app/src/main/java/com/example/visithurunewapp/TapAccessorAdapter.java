package com.example.visithurunewapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TapAccessorAdapter extends FragmentPagerAdapter {
    public TapAccessorAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position)
        {
            case 0:
                ChatsFragment chatFragment = new ChatsFragment();
                return chatFragment;
            case 1:
                GroupsFragment groupsFragment = new GroupsFragment();
                return groupsFragment;
//            case 2:
//                ContactsFragment contactsFragment = new ContactsFragment();
//                return contactsFragment;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position)
        {
            case 0:
                return "Chats";
            case 1:
                return "Group";
//            case 2:
//                return "Contacts";
            default:
                return null;
        }
    }
}
