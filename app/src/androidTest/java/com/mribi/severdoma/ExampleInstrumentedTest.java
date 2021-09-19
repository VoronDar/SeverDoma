package com.mribi.severdoma;

import android.app.Instrumentation;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import com.mribi.severdoma.Activities.AddNewBisiness;

import org.junit.Test;
import org.junit.runner.RunWith;



import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        assertEquals("com.mribi.severdoma", appContext.getPackageName());
    }

    @Test
    public void checkMaps(){
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        instrumentation.callActivityOnCreate(new AddNewBisiness(), null);
    }


}
