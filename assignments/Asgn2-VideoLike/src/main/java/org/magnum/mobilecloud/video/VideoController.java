/*
 * 
 * Copyright 2014 Jules White
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package org.magnum.mobilecloud.video;

import com.google.common.collect.Lists;
import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.magnum.mobilecloud.video.exceptions.BadRequestException;
import org.magnum.mobilecloud.video.exceptions.NoSuchTypeException;
import org.magnum.mobilecloud.video.exceptions.ResourceNotFoundException;
import org.magnum.mobilecloud.video.repository.Video;
import org.magnum.mobilecloud.video.repository.VideoRepository;
import org.magnum.mobilecloud.video.types.ExistType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;

@Controller
public class VideoController {
    private static final int ZERO_LIKES = 0;
    private static final String VIDEO_ID_PATH = "/{id}";
    private static final String VIDEO_LIKE_PATH = VideoSvcApi.VIDEO_SVC_PATH + VIDEO_ID_PATH + "/like";
    private static final String VIDEO_LIKED_BY_PATH = VideoSvcApi.VIDEO_SVC_PATH + VIDEO_ID_PATH + "/likedby";
    private static final String VIDEO_UNLIKE_PATH = VideoSvcApi.VIDEO_SVC_PATH + VIDEO_ID_PATH + "/unlike";
    private static final String VIDEO_GET_PATH = VideoSvcApi.VIDEO_SVC_PATH + VIDEO_ID_PATH;
    @Autowired
    private VideoRepository videos;

    @RequestMapping(value = "/go", method = RequestMethod.GET)
    public @ResponseBody String goodLuck() {
        return "Good Luck!";
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
    public @ResponseBody Video addVideo(@RequestBody Video v) {
        v.setLikes(ZERO_LIKES);
        videos.save(v);
        return v;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> getVideoList() {
        return Lists.newArrayList(videos.findAll());
    }

    @RequestMapping(value = VIDEO_GET_PATH, method = RequestMethod.GET)
    public @ResponseBody Video getVideoById(@PathVariable long id) {
        return getVideo(id);
    }

    private Video getVideo(long id) {
        final Video video = videos.findOne(id);
        if (video == null) {
            throw new ResourceNotFoundException();
        }
        return video;
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title) {
        return videos.findByName(title);
    }

    @RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<Video> findByDurationLessThan(
            @RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration) {
        return videos.findByDurationLessThan(duration);
    }

    @RequestMapping(value = VIDEO_LIKE_PATH, method = RequestMethod.POST)
    public @ResponseBody ResponseEntity likeVideo(@PathVariable long id, Principal principal) {
        final Video video = getVideo(id);
        final Set<String> likeUsernames = video.getLikesUsernames();
        checkVideoForLikes(likeUsernames, ExistType.SHOULD_EXIST, principal.getName());
        updateVideoAndLikes(video, likeUsernames, ExistType.SHOULD_EXIST, principal.getName());
        return new ResponseEntity(HttpStatus.OK);
    }

    private void checkVideoForLikes(Set likeUsernames, ExistType shouldExist, String name) {
        switch (shouldExist) {
        case SHOULD_EXIST: {
            if (likeUsernames.contains(name)) {
                throw new BadRequestException();
            }
            break;
        }
        case SHOULD_NOT_EXIST: {
            if (!likeUsernames.contains(name)) {
                throw new BadRequestException();
            }
            break;
        }
        default: {
            throw new NoSuchTypeException();
        }
        }

    }

    @RequestMapping(value = VIDEO_UNLIKE_PATH, method = RequestMethod.POST)
    public @ResponseBody ResponseEntity unlikeVideo(@PathVariable long id, Principal principal) {
        final Video video = getVideo(id);
        final Set<String> likeUsernames = video.getLikesUsernames();
        checkVideoForLikes(likeUsernames, ExistType.SHOULD_NOT_EXIST, principal.getName());
        updateVideoAndLikes(video, likeUsernames, ExistType.SHOULD_NOT_EXIST, principal.getName());
        return new ResponseEntity(HttpStatus.OK);
    }

    @RequestMapping(value = VIDEO_LIKED_BY_PATH, method = RequestMethod.GET)
    public @ResponseBody Collection<String> getUsersWhoLikedVideo(@PathVariable long id) {
        final Video video = getVideo(id);
        return video.getLikesUsernames();
    }

    private void updateVideoAndLikes(Video video, Set<String> likeUsernames, ExistType shouldExist, String name) {
        switch (shouldExist) {
        case SHOULD_EXIST: {
            likeUsernames.add(name);
            video.setLikesUsernames(likeUsernames);
            video.setLikes(likeUsernames.size());
            videos.save(video);
            break;
        }
        case SHOULD_NOT_EXIST: {
            likeUsernames.remove(name);
            video.setLikesUsernames(likeUsernames);
            video.setLikes(likeUsernames.size());
            videos.save(video);
            break;
        }
        default: {
            throw new NoSuchTypeException();
        }
        }
    }
}
