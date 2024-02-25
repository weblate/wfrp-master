import {firestore, storage} from 'firebase-admin';
import * as t from "io-ts";
import {characterChange} from "../characterChange";
import { v4 as uuidv4 } from "uuid";
import {generateAvatarUrl, getAvatarPath, METADATA} from "../avatar";

const RequestBody = t.interface({
    partyId: t.string,
    characterId: t.string,
    newName: t.string,
});

type Character = {
    avatarUrl: string | null;
}

export const duplicateCharacter = characterChange(RequestBody, async (body, character) => {
    const batch = firestore().batch();
    const newCharacterId = uuidv4();
    const newCharacter = character.parent.doc(newCharacterId);
    const characterData = (await character.get()).data() as Character;

    const newCharacterData =         {
        ...characterData,
        id: newCharacterId,
        name: body.newName,
        avatarUrl: await copyAvatar(characterData.avatarUrl, body.partyId, body.characterId, newCharacterId),
    };

    console.log("New character data", newCharacterData);

    batch.set(newCharacter, newCharacterData);

    const collections = await character.listCollections();

    await Promise.all(
        collections.map(async (collection) => {
            await Promise.all(
                (await collection.listDocuments()).map(async (document) => {
                    batch.set(
                        newCharacter.collection(collection.id).doc(document.id),
                        (await document.get()).data(),
                    );
                })
            );
        })
    );

    await batch.commit();

    return {
        status: "success",
    };
})

const copyAvatar = async (
    avatarUrl: string | null,
    partyId: string,
    characterId: string,
    newCharacterId: string,
): Promise<string | null> => {
    if (avatarUrl === null) {
        return null;
    }

    const bucket = storage().bucket();

    try {
        const sourceFile = bucket.file(getAvatarPath(partyId, characterId));

        if (!(await sourceFile.exists())[0]) {
            return null;
        }

        const copyResponse = await sourceFile.copy(
            bucket.file(getAvatarPath(partyId, newCharacterId)),
            {metadata: METADATA}
        );

        return await generateAvatarUrl(copyResponse, bucket);
    } catch (e) {
        console.error(e);
        return null;
    }
}
