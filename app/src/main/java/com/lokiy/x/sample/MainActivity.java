package com.lokiy.x.sample;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.List;

import com.lokiy.x.XConfig;
import com.lokiy.x.XParser;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG).setAction("Action", null).show();
			}
		});

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.setDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		XParser.INSTANCE.init(XConfig.createDefaultConfig(this));
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		} else {
			super.onBackPressed();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

		if (id == R.id.nav_camara) {
			// Handle the camera action
		} else {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
			List<Fragment> fragments = getSupportFragmentManager().getFragments();
			for (int i = 0, size = fragments == null ? 0 : fragments.size(); i < size; i++) {
				fragmentTransaction.hide(fragments.get(i));
			}
			if (id == R.id.x_parser_adapter) {
				addFragment(fragmentTransaction, XParserAdapterFragment.class);
			} else if (id == R.id.x_parser_db) {
				addFragment(fragmentTransaction, XDBHelperFragment.class);
			} else if (id == R.id.nav_download) {
				addFragment(fragmentTransaction, DownloadHelperFragment.class);
			} else if (id == R.id.nav_share) {

			} else if (id == R.id.nav_send) {

			}
			fragmentTransaction.commit();
		}

		DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	private Fragment addFragment(FragmentTransaction fragmentTransaction, Class<? extends Fragment> clazz) {
		Fragment fragment = null;
		List<Fragment> fragments = getSupportFragmentManager().getFragments();
		for (int i = 0, size = fragments == null ? 0 : fragments.size(); i < size; i++) {
			Fragment f = fragments.get(i);
			if (f.getClass() == clazz) {
				fragment = f;
				break;
			}
		}
		if (fragment == null) {
			fragment = Fragment.instantiate(this, clazz.getName());
			fragmentTransaction.add(R.id.main_container, fragment);
		} else {
			fragmentTransaction.show(fragment);
		}
		return fragment;
	}
}
