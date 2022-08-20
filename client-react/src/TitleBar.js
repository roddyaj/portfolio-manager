function TitleBar(props) {
	const { accounts, selectedAccount, setSelectedAccount, portfolio, requestPortfolio } = props;

	const accountOptions = accounts.map((account) => {
		return (<option key={account} value={account}>{account}</option>);
	});

	return (
		<div className="pm-block">
			<div style={{ display: "flex", alignItems: "center" }}>
				<div style={{ marginRight: 12 }}>
					<select name="accounts" id="accounts" value={selectedAccount != null ? selectedAccount : ""} onChange={(e) => setSelectedAccount(e.target.value)}>
						{accountOptions}
					</select>
				</div>

				<button type="button" style={{ marginRight: 12 }} onClick={() => requestPortfolio(selectedAccount)}>Refresh</button>

				{
					portfolio && (
						<div>
							<span style={{ marginRight: 12 }}>Balance: {portfolio.balance.toLocaleString()}</span>
							<span style={{ marginRight: 12 }}>Cash: {portfolio.cash.toLocaleString()}</span>
						</div>
					)
				}
			</div>
		</div>
	);
}

export default TitleBar;
