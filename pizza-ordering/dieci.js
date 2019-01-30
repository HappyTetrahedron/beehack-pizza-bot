const DIECI_HOME_PAGE = 'https://www.dieci.ch/en/index'

async function findDropdownToggle(page, articleNumber) {
    const articlePanel = await page.waitForSelector(`.article-container[data-article-number="${articleNumber}"]`);
    return await articlePanel.$('.dropdown-toggle');
}

async function selectArticleInDropdown(page, dropdownToggle, articleId) {
    await dropdownToggle.click();

    const articleItem = await page.waitForSelector(`.dropdown-menu .choose-article[data-article-id="${articleId}"]`);
    await articleItem.click();
}

async function addToCart(page, articleId) {
    const addToCartButton = await page.waitForSelector(`.container-choose-article:not(.choose-toppings) .choose-article[data-article-id="${articleId}"]`, {
        visible: true,
    })
    await addToCartButton.click()
    await page.waitFor(3000)
}

module.exports = {

    async goToMenuPage(page, { postCode }) {
        console.log(`Going to ${DIECI_HOME_PAGE} (zip code: ${postCode})`);
        await page.goto(DIECI_HOME_PAGE);
        await page.type('#plzEntry', postCode);
        await page.waitFor(1000);
        await page.click('#orderPizza');
        await page.waitForSelector('.article-container');
    },

    async addToShoppingCart(page, { articleId, commodityGroupId, articleNumber }) {
        console.log('Ordering article', articleId);
        await page.click(`li[commoditygroup_id="${commodityGroupId}"] a`);

        const dropdownToggle = await findDropdownToggle(page, articleNumber);
        if (dropdownToggle) {
            await selectArticleInDropdown(page, dropdownToggle, articleId);
        }

        await addToCart(page, articleId);
    },

    async goToShoppingCart(page) {
        console.log('Navigating to shopping cart');
        await page.waitForSelector(".Reached_minOrderPrice", { visible: true, timeout: 10000 });
        await page.click(".Reached_minOrderPrice");
        await page.waitForSelector(".goto-shopping-cart .btn", { visible: true, timeout: 10000 });
        await page.click(".goto-shopping-cart .btn");
        await page.waitForSelector('[name="order_comment"]', { visible: true, timeout: 10000 });
    },

    async fillPersonalDataForm(page, personalData, creditCard) {
        console.log('Filling in personal data form');

        await page.type('#firstname', personalData.firstName);
        await page.type('#lastname', personalData.lastName);
        await page.type('#communication_email', personalData.email);
        await page.type('#communication_phonenumber', personalData.phone);

        await page.click('label[for="radio_isCompany_1"]')
        await page.click('label[for="radio_gender_m"]')

        await page.type('input[name="customerdata[customer_companyname]"]', personalData.company);
        await page.type('input[name="customerdata[customer_department]"]', personalData.department);

        await page.type('#input_zip', personalData.postalCode);
        await page.type('#input_city', personalData.city);
        await page.waitFor(1000)
        await page.type('#input_city', String.fromCharCode(13));
        await page.type('#input_street', personalData.street);
        await page.waitFor(2000)
        await page.type('#input_street', String.fromCharCode(13));
        await page.type('#input_streetnumber', personalData.streetNumber);

        await page.click('label[for="radio_delivery_now_1"]')
        await page.click('input[name="docut"]')

        if (creditCard) {
            console.log('Selecting credit card');
            await page.click('label[for="cb_payment_30"]')
        } else {
            await page.click('label[for="cb_payment_1"]')
        }
        await page.click('#cb_agb_warning')

        await page.screenshot({ path: 'form.png', fullPage: true });
    },

    async executeOrder66(page) {
        console.log("Submitting order");
        await page.click('button[data-rel="checkout_send"]');
        await page.waitFor(4000);
        console.log("Taking screenshot final.png");
        await page.screenshot({ path: 'final.png', fullPage: true });
        await page.waitFor(4000);
    },

}