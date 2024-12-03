package com.example.momentory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.momentory.databinding.ActivitySelectLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class SelectLocationActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    private var selectedLocation: LatLng? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySelectLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // SupportMapFragment 가져오기
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 권한 요청
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        // 저장 버튼 클릭하면 위치가 반환되도록
        binding.confirmButton.setOnClickListener {
            if (selectedLocation != null) {
                val geocoder = Geocoder(this)
                val addresses = geocoder.getFromLocation(
                    selectedLocation!!.latitude,
                    selectedLocation!!.longitude,
                    1
                )

                if (addresses != null && addresses.isNotEmpty()) {
                    val address = addresses[0].getAddressLine(0)

                    val resultIntent = Intent()
                    resultIntent.putExtra("location", address)
                    setResult(RESULT_OK, resultIntent)
                    finish()  // SelectLocationActivity 종료
                }
            } else {
                Toast.makeText(this, "지도를 클릭하여 위치를 선택하세요.", Toast.LENGTH_SHORT).show()
            }
        }


    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 권한 확인 후 내 위치 활성화
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            // 권한 요청
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }

        // 기본 위치: 서울 청파로47길 100
        val defaultLocation = LatLng(37.560627, 126.972332)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 16f)) // 확대 수준 설정

        // 지도 클릭 시 마커 추가
        googleMap.setOnMapClickListener { latLng ->
            // 마커 초기화 (기존 마커 제거)
            googleMap.clear()

            // 새 마커 추가
            googleMap.addMarker(MarkerOptions().position(latLng).title("선택된 위치"))

            // 선택한 위치 저장
            selectedLocation = latLng
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return
                }
                googleMap.isMyLocationEnabled = true
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
