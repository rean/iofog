package org.eclipse.iofog.process_manager;

public class UkDetails
{
   private String _appId = "";
   private String _imageName = "";
   private String _ipAddress = "";

   public UkDetails()
   {}

   public UkDetails(String appId, String imageName, String ipAddress) {
      _appId = appId;
      _imageName = imageName;
      _ipAddress = ipAddress;
   }

   public String getAppId() { return _appId; }
   public void setAppId(String appId) { _appId = appId; }
   public String getImageName() { return _imageName; }
   public void setImageName(String imageName) { _imageName = imageName; }
   public String getIpAddress() { return _ipAddress; }
   public void setIpAddress(String ipAddress) { _ipAddress = ipAddress; }
}
