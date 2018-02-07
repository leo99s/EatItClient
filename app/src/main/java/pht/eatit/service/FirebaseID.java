package pht.eatit.service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import pht.eatit.global.Global;
import pht.eatit.model.Token;

public class FirebaseID extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        String tokenRefreshed = FirebaseInstanceId.getInstance().getToken();

        if(Global.activeUser != null){
            updateToken(tokenRefreshed);
        }
    }

    private void updateToken(String tokenRefreshed) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference token = database.getReference("Token");
        Token child = new Token(tokenRefreshed, false);
        token.child(Global.activeUser.getPhone()).setValue(child);
    }
}