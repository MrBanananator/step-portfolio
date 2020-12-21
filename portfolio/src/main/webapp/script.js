// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

async function putComments() {
    const params = new URLSearchParams();
    const languageCode = document.getElementById('language').value;
    console.log(languageCode);
    params.append('languageCode', languageCode);

    const response = await fetch('/data', {method: 'POST', body: params});
    const json = await response.json();

    var maxComments = document.getElementById("comment-num").value;

    document.getElementById("comments-container").innerHTML = "";

    var element = document.getElementById("comments-container");
    for (i = 0; i < json.length && i < maxComments; i++) {
        element.appendChild(createCommentElement(json[i]));
    }
}

/** Creates an <p> element containing text. */
function createCommentElement(comment) {
    const commentElement = document.createElement('li');
    commentElement.className = 'comment';

    const contentElement = document.createElement('p');
    contentElement.innerText = comment.content;

    const deleteButton = document.createElement('button');
    deleteButton.innerText = 'Delete';
    deleteButton.addEventListener('click', () => {
        deleteComment(comment);
        commentElement.remove();
    });

    commentElement.appendChild(contentElement);
    commentElement.appendChild(deleteButton);
    return commentElement;
}

async function deleteComment(comment) {
    const params = new URLSearchParams();
    params.append('id', comment.id);
    await fetch('/delete-data', {method: 'POST', body: params});
    putComments();
}

/** Creates a map and adds it to the page. */
function createMap() {
    const map = new google.maps.Map(
        document.getElementById('map'),
        {
            center: {lat: -28.2157, lng: 152.0282}, 
            zoom: 13, 
            mapTypeId: 'satellite'
        },
    );
}