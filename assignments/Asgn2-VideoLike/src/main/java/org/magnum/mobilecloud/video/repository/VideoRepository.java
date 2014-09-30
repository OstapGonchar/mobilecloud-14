package org.magnum.mobilecloud.video.repository;

import java.util.Collection;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

/**
 * An interface for a repository that can store Video
 * objects and allow them to be searched by title.
 * 
 * @author jules
 *
 */
@RepositoryRestResource(path = VideoSvcApi.VIDEO_SVC_PATH)
public interface VideoRepository extends CrudRepository<Video, Long>{

	// Find all videos with a matching title (e.g., Video.name)
	public Collection<Video> findByName(@Param(VideoSvcApi.TITLE_PARAMETER) String title);

    public Collection<Video> findByDurationLessThan(@Param(VideoSvcApi.DURATION_PARAMETER) long maxduration);

}
