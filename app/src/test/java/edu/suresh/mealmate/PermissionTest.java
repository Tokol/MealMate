package edu.suresh.mealmate;

import android.Manifest;
import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.Shadows;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 33)
public class PermissionTest {

    @Test
    public void testPermissionGranted() {
        Context context = ApplicationProvider.getApplicationContext();
        Shadows.shadowOf((AccessibilityService) context).grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION);

        int permissionCheck = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        assertEquals(PackageManager.PERMISSION_GRANTED, permissionCheck);
    }
}
