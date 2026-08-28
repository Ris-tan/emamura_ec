(() => {
    const searchButton = document.querySelector('[data-address-search]');
    if (!searchButton) {
        return;
    }

    const postalCodeInput = document.getElementById(searchButton.dataset.postalCodeInput);
    const prefectureInput = document.getElementById(searchButton.dataset.prefectureInput);
    const cityInput = searchButton.dataset.cityInput
        ? document.getElementById(searchButton.dataset.cityInput)
        : null;
    const addressLineInput = searchButton.dataset.addressLineInput
        ? document.getElementById(searchButton.dataset.addressLineInput)
        : null;
    const message = document.getElementById(searchButton.dataset.message);

    if (!postalCodeInput || !prefectureInput || !message) {
        return;
    }

    searchButton.addEventListener('click', async () => {
        const postalCode = postalCodeInput.value.replace(/[^0-9]/g, '');
        message.textContent = '';

        if (!/^\d{7}$/.test(postalCode)) {
            message.textContent = '郵便番号を7桁で入力してください。';
            return;
        }

        try {
            const response = await fetch(
                'https://zipcloud.ibsnet.co.jp/api/search?zipcode=' + encodeURIComponent(postalCode));
            if (!response.ok) {
                throw new Error('Address search failed');
            }

            const data = await response.json();
            if (String(data.status) !== '200' || !Array.isArray(data.results) || data.results.length === 0) {
                message.textContent = '住所が見つかりません。住所を手入力してください。';
                return;
            }

            const result = data.results[0];
            const cityAddress = (result.address2 || '') + (result.address3 || '');
            prefectureInput.value = result.address1 || '';

            if (cityInput) {
                cityInput.value = cityAddress;
            }
            if (addressLineInput) {
                // UserAddress.address_line is one column, so the postal lookup seeds the city part here.
                // The user can append the street and building details before submitting.
                addressLineInput.value = cityAddress;
            }
            message.textContent = '住所を入力しました。番地・建物名を続けて入力してください。';
        } catch (error) {
            message.textContent = '住所検索に失敗しました。住所を手入力してください。';
        }
    });
})();
