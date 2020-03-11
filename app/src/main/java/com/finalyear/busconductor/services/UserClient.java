package com.finalyear.busconductor.services;

import android.app.Application;

import com.finalyear.busconductor.model.Conductor;

public class UserClient extends Application {
    private Conductor conductor = null;

    public Conductor getConductor() {
        return conductor;
    }

    public void setConductor(Conductor conductor) {
        this.conductor = conductor;
    }

}
