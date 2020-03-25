package com.gmail.leonard.spring.Frontend.Views;

import com.gmail.leonard.spring.Backend.Language.PageTitleGen;
import com.gmail.leonard.spring.Backend.UserData.SessionData;
import com.gmail.leonard.spring.Backend.UserData.UIData;
import com.gmail.leonard.spring.Frontend.Components.CustomNotification;
import com.gmail.leonard.spring.Frontend.Layouts.PageLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Main;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route(value = "discordlogin")
public class DiscordLogin extends PageLayout implements HasUrlParameter<String> {

    private SessionData sessionData;
    private UIData uiData;

    public DiscordLogin(@Autowired SessionData sessionData, @Autowired UIData uiData) {
        this.sessionData = sessionData;
        this.uiData = uiData;
    }

    @Override
    public void setParameter(BeforeEvent event,
                             @OptionalParameter String parameter) {

        Location location = event.getLocation();
        QueryParameters queryParameters = location
                .getQueryParameters();

        Map<String, List<String>> parametersMap =
                queryParameters.getParameters();

        if (parametersMap != null && parametersMap.containsKey("code") && parametersMap.containsKey("state")) {
            String code = parametersMap.get("code").get(0);
            String state = parametersMap.get("state").get(0);

            if (!sessionData.login(code, state, uiData)) {
                CustomNotification.showError(getTranslation("login.error"));
            }
        }

        Class<? extends Component> resumeClass = sessionData.isLoggedIn() ? (Class<? extends Component>) sessionData.getCurrentTarget() : HomeView.class;

        UI.getCurrent().navigate(resumeClass);
        event.rerouteTo(resumeClass);
    }

}
