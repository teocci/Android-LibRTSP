## RTSP Client-Server

This is a very simple and straight-forward implementation to record and play real time video in an Android devices. It can create both TCP and UDP client or server. It can be used to test any server or client that uses TCP or UDP protocol to communicate. 

### Disclaimer

This repository contains sample code intended to demonstrate the capabilities of the Exoplayer. The current version of the code only supports RTP over UDP as the transport protocol and only decodes H264 encoded video. It is not intended to be used as-is in applications as a library dependency, and will not be maintained as such. Bug fix contributions are welcome, but issues and feature requests will not be addressed.

### Design:

This project has 4 modules: `LibRtsp`, `RtspCapturer`, `RtspServer`, and `RtspViewer`. 

The first module, `LibRtsp`, provides a basic implementation for RTSP and RTP protocol communication. Also, encapsulates the Exoplayer as decoder in the `RtspPlayer` class.

`RtspCapturer` implements a camera and audio recorder using MediaCodec library from Android. This module encodes the video and audio using H264 and AAC respectively into RTSP Packets. Then those packets are send to the `RtspServer` using RTP.

`Camera/AudioRecord -> MediaCodec -> H264/AAC -> RTP`

`RtspSever` implements a session manager to handle the client connections. Also receives the incoming audio and video using RTP and re-send the steaming using RTSP to the clients.

Finally, `RtspViewer` is a basic RTSP client that plays the RTSP steaming from the `RTSPServer`.

`RtspCapturer --udp-> RtspServer --udp/tcp-> RtspViewer (RtspPlayer) is build on ExoPlayer`

### Contributing
If you would like to contribute code, you can do so through GitHub by forking the repository and sending a pull request.
When submitting code, please make every effort to follow existing conventions and style in order to keep the code as readable as possible.

### Pre-requisites

- Android SDK 25
- Android Build Tools v25.0.2
- Android Support Repository


## Credits

This project was based on the huge knowledge learn from the **libstreaming** project.

- [libstreaming][1]

## License

The code supplied here is covered under the MIT Open Source License..

[1]: https://github.com/fyhertz/libstreaming



