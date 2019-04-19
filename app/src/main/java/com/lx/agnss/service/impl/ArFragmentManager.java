package com.lx.agnss.service.impl;

import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.Set;

public class ArFragmentManager extends ArFragment {

    public ArFragmentManager() {
    }

    @Override
    protected Config getSessionConfiguration(Session session) {
        return super.getSessionConfiguration(session);
    }

    @Override
    public boolean isArRequired() {
        return super.isArRequired();
    }

    @Override
    protected void handleSessionException(UnavailableException sessionException) {
        super.handleSessionException(sessionException);
    }

    @Override
    protected Set<Session.Feature> getSessionFeatures() {
        return super.getSessionFeatures();
    }
}
